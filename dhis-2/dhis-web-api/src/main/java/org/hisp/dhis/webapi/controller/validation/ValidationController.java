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
package org.hisp.dhis.webapi.controller.validation;

import static org.hisp.dhis.dxf2.webmessage.WebMessageUtils.jobConfigurationReport;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.hisp.dhis.category.CategoryOptionCombo;
import org.hisp.dhis.category.CategoryService;
import org.hisp.dhis.common.OpenApi;
import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.dxf2.webmessage.WebMessage;
import org.hisp.dhis.feedback.ConflictException;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.scheduling.JobConfiguration;
import org.hisp.dhis.scheduling.JobExecutionService;
import org.hisp.dhis.scheduling.JobProgress;
import org.hisp.dhis.scheduling.JobType;
import org.hisp.dhis.security.Authorities;
import org.hisp.dhis.security.RequiresAuthority;
import org.hisp.dhis.validation.ValidationAnalysisParams;
import org.hisp.dhis.validation.ValidationService;
import org.hisp.dhis.validation.ValidationSummary;
import org.hisp.dhis.webapi.controller.datavalue.DataValidator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Lars Helge Overland
 */
@OpenApi.Document(entity = DataSet.class)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/validation")
public class ValidationController {

  private final ValidationService validationService;
  private final CategoryService categoryService;
  private final JobExecutionService jobExecutionService;
  private final DataValidator dataValidator;

  @GetMapping("/dataSet/{ds}")
  public ValidationSummary validate(
      @PathVariable String ds,
      @RequestParam String pe,
      @RequestParam String ou,
      @RequestParam(required = false) String aoc,
      @RequestParam(required = false) String cc,
      @RequestParam(required = false) String cp) {
    DataSet dataSet = dataValidator.getAndValidateDataSet(ds);
    Period period = dataValidator.getAndValidatePeriod(pe);
    OrganisationUnit orgUnit = dataValidator.getAndValidateOrganisationUnit(ou);
    CategoryOptionCombo attributeOptionCombo = getAttributeOptionCombo(aoc, cc, cp);

    ValidationSummary summary = new ValidationSummary();

    ValidationAnalysisParams params =
        validationService
            .newParamsBuilder(dataSet, orgUnit, period)
            .withAttributeOptionCombo(attributeOptionCombo)
            .build();

    summary.setValidationRuleViolations(
        validationService.validationAnalysis(params, JobProgress.noop()));
    summary.setCommentRequiredViolations(
        validationService.validateRequiredComments(dataSet, period, orgUnit, attributeOptionCombo));

    return summary;
  }

  @RequestMapping(
      value = "/sendNotifications",
      method = {RequestMethod.PUT, RequestMethod.POST})
  @RequiresAuthority(anyOf = Authorities.F_RUN_VALIDATION)
  public WebMessage runValidationNotificationsTask() throws ConflictException {
    JobConfiguration config = new JobConfiguration(JobType.VALIDATION_RESULTS_NOTIFICATION);

    jobExecutionService.executeOnceNow(config);

    return jobConfigurationReport(config);
  }

  /**
   * Returns an attribute option combination based on the given identifiers, or the default
   * attribute option combination if all identifiers are blank.
   *
   * @param aoc the attribute option combination identifier.
   * @param cc the category combination identifier.
   * @param cp the category option identifier.
   * @return a {@link CategoryOptionCombo}.
   */
  private CategoryOptionCombo getAttributeOptionCombo(String aoc, String cc, String cp) {
    CategoryOptionCombo attributeOptionCombo = null;

    if (StringUtils.isNotBlank(aoc)) {
      attributeOptionCombo = dataValidator.getAndValidateCategoryOptionCombo(aoc);
    } else if (StringUtils.isNoneBlank(cc, cp)) {
      attributeOptionCombo = dataValidator.getAndValidateAttributeOptionCombo(cc, cp);
    } else {
      attributeOptionCombo = categoryService.getDefaultCategoryOptionCombo();
    }

    return attributeOptionCombo;
  }
}
