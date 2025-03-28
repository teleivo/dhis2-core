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
package org.hisp.dhis.analytics.table.scheduling;

import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.hisp.dhis.analytics.AnalyticsTableGenerator;
import org.hisp.dhis.analytics.AnalyticsTableUpdateParams;
import org.hisp.dhis.scheduling.Job;
import org.hisp.dhis.scheduling.JobConfiguration;
import org.hisp.dhis.scheduling.JobProgress;
import org.hisp.dhis.scheduling.JobType;
import org.hisp.dhis.scheduling.parameters.AnalyticsJobParameters;
import org.springframework.stereotype.Component;

/**
 * Job for full analytics table update.
 *
 * @author Lars Helge Overland
 */
@Component
@RequiredArgsConstructor
public class AnalyticsTableJob implements Job {
  private final AnalyticsTableGenerator analyticsTableGenerator;

  @Override
  public JobType getJobType() {
    return JobType.ANALYTICS_TABLE;
  }

  @Override
  public void execute(JobConfiguration jobConfiguration, JobProgress progress) {
    AnalyticsJobParameters parameters =
        (AnalyticsJobParameters) jobConfiguration.getJobParameters();

    AnalyticsTableUpdateParams params =
        AnalyticsTableUpdateParams.newBuilder()
            .lastYears(parameters.getLastYears())
            .skipResourceTables(parameters.isSkipResourceTables())
            .skipOutliers(parameters.isSkipOutliers())
            .skipTableTypes(parameters.getSkipTableTypes())
            .skipPrograms(parameters.getSkipPrograms())
            .jobId(jobConfiguration)
            .startTime(new Date())
            .build();

    analyticsTableGenerator.generateAnalyticsTables(params, progress);
  }
}
