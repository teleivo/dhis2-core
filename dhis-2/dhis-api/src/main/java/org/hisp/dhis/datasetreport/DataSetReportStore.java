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
package org.hisp.dhis.datasetreport;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.period.Period;

/**
 * @author Lars Helge Overland
 */
public interface DataSetReportStore {
  String SEPARATOR = "-";

  /**
   * Get a mapping from dimensional identifiers to aggregated values.
   *
   * @param dataSet the data set.
   * @param periods the periods.
   * @param unit the organisation unit.
   * @param filters the filters on the analytics dimension format, e.g.
   *     <dim-id>:<dim-item>;<dim-item>
   * @return a mapping from dimensional identifiers to aggregated values.
   */
  Map<String, Object> getAggregatedValues(
      DataSet dataSet, List<Period> periods, OrganisationUnit unit, Set<String> filters);

  /**
   * Get a mapping from dimensional identifiers to aggregated sub-total values.
   *
   * @param dataSet the data set.
   * @param periods the periods.
   * @param unit the organisation unit.
   * @param filters the filters on the analytics dimension format, e.g.
   *     <dim-id>:<dim-item>;<dim-item>
   * @return a mapping from dimensional identifiers to aggregated sub-total values.
   */
  Map<String, Object> getAggregatedSubTotals(
      DataSet dataSet, List<Period> periods, OrganisationUnit unit, Set<String> filters);

  /**
   * Get a mapping from dimensional identifiers to aggregated total values.
   *
   * @param dataSet the data set.
   * @param periods the periods.
   * @param unit the organisation unit.
   * @param filters the filters on the analytics dimension format, e.g.
   *     <dim-id>:<dim-item>;<dim-item>
   * @return a mapping from dimensional identifiers to aggregated total values.
   */
  Map<String, Object> getAggregatedTotals(
      DataSet dataSet, List<Period> periods, OrganisationUnit unit, Set<String> filters);

  /**
   * Get a mapping from dimensional identifiers to aggregated indicator values.
   *
   * @param dataSet the data set.
   * @param periods the periods.
   * @param unit the organisation unit.
   * @param filters the filters on the analytics dimension format, e.g.
   *     <dim-id>:<dim-item>;<dim-item>
   * @return a mapping from dimensional identifiers to aggregated indicator values.
   */
  Map<String, Object> getAggregatedIndicatorValues(
      DataSet dataSet, List<Period> periods, OrganisationUnit unit, Set<String> filters);
}
