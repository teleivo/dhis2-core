/*
 * Copyright (c) 2004-2022, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors 
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.hisp.dhis.webapi.controller;

import static org.hisp.dhis.dxf2.webmessage.WebMessageUtils.conflict;
import static org.hisp.dhis.security.Authorities.F_GENERATE_MIN_MAX_VALUES;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import org.hisp.dhis.common.OpenApi;
import org.hisp.dhis.dataanalysis.MinMaxDataAnalysisService;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.dataset.DataSetService;
import org.hisp.dhis.datavalue.DataValue;
import org.hisp.dhis.dxf2.webmessage.WebMessageException;
import org.hisp.dhis.minmax.MinMaxDataElementService;
import org.hisp.dhis.minmax.MinMaxValueParams;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.security.RequiresAuthority;
import org.hisp.dhis.setting.SystemSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * min max value endpoint to to generate and remove min max values
 *
 * @author Joao Antunes
 */
@OpenApi.Document(
    entity = DataValue.class,
    classifiers = {"team:platform", "purpose:data"})
@Controller
@RequestMapping("/api/minMaxValues")
public class MinMaxValueGenerationController {

  @Autowired private MinMaxDataElementService minMaxDataElementService;

  @Autowired private MinMaxDataAnalysisService minMaxDataAnalysisService;

  @Autowired private DataSetService dataSetService;

  @Autowired private OrganisationUnitService organisationUnitService;

  @PostMapping(consumes = APPLICATION_JSON_VALUE)
  @RequiresAuthority(anyOf = F_GENERATE_MIN_MAX_VALUES)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void generateMinMaxValue(
      @RequestBody MinMaxValueParams minMaxValueParams, SystemSettings settings)
      throws WebMessageException {
    List<String> dataSets = minMaxValueParams.getDataSets();
    String organisationUnitId = minMaxValueParams.getOrganisationUnit();

    if (dataSets == null || dataSets.isEmpty()) {
      throw new WebMessageException(conflict(" No datasets defined"));
    }

    OrganisationUnit organisationUnit =
        this.organisationUnitService.getOrganisationUnit(organisationUnitId);
    if (organisationUnit == null) {
      throw new WebMessageException(conflict(" No valid organisation unit"));
    }

    Collection<DataElement> dataElements = new HashSet<>();

    for (String dataSetId : dataSets) {
      DataSet dataSet = this.dataSetService.getDataSet(dataSetId);
      dataElements.addAll(dataSet.getDataElements());
    }

    double factor = settings.getFactorOfDeviation();

    minMaxDataAnalysisService.generateMinMaxValues(organisationUnit, dataElements, factor);
  }

  @DeleteMapping("/{ou}")
  @RequiresAuthority(anyOf = F_GENERATE_MIN_MAX_VALUES)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void removeMinMaxValue(
      @PathVariable("ou") String organisationUnitId, @RequestParam("ds") List<String> dataSetIds)
      throws WebMessageException {
    if (dataSetIds == null || dataSetIds.isEmpty()) {
      throw new WebMessageException(conflict(" No datasets defined"));
    }

    OrganisationUnit organisationUnit =
        this.organisationUnitService.getOrganisationUnit(organisationUnitId);
    if (organisationUnit == null) {
      throw new WebMessageException(conflict(" No valid organisation unit"));
    }

    Collection<DataElement> dataElements = new HashSet<>();

    for (String dataSetId : dataSetIds) {
      DataSet dataSet = this.dataSetService.getDataSet(dataSetId);
      dataElements.addAll(dataSet.getDataElements());
    }

    minMaxDataElementService.removeMinMaxDataElements(dataElements, organisationUnit);
  }
}
