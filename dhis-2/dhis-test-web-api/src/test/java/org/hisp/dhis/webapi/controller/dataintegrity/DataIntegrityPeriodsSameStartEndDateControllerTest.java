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
package org.hisp.dhis.webapi.controller.dataintegrity;

import java.time.ZonedDateTime;
import java.util.Date;
import org.hisp.dhis.period.MonthlyPeriodType;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodService;
import org.hisp.dhis.period.PeriodType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Test the metadata check for periods which have the same period type and which have the same start
 * and end date. The test scenario is not possible to recreate in current versions of DHIS2 because
 * of a unique constraint placed on the period type, start date and end date. Here, we will only
 * test that the check actually runs.
 *
 * <p>{@see
 * dhis-2/dhis-services/dhis-service-administration/src/main/resources/data-integrity-checks/periods/periods_same_start_date_period_type.yaml}
 *
 * @author Jason P. Pickering
 */
class DataIntegrityPeriodsSameStartEndDateControllerTest
    extends AbstractDataIntegrityIntegrationTest {
  @Autowired private PeriodService periodService;

  private static final String check = "periods_same_start_date_period_type";

  @Test
  void testNoBadPeriodsExist() {

    PeriodType periodType = new MonthlyPeriodType();
    Date date_future = Date.from(ZonedDateTime.now().plusYears(1).plusDays(1).toInstant());
    Period periodA = periodType.createPeriod(date_future);
    periodService.addPeriod(periodA);

    Date date_past = Date.from(ZonedDateTime.now().minusMonths(5).toInstant());
    Period periodC = periodType.createPeriod(date_past);
    periodService.addPeriod(periodC);
    dbmsManager.clearSession();

    assertHasNoDataIntegrityIssues("periods", check, true);
  }
}
