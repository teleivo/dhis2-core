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
package org.hisp.dhis.test.e2e.actions.analytics;

import org.hisp.dhis.test.e2e.actions.RestApiActions;
import org.hisp.dhis.test.e2e.dto.ApiResponse;
import org.hisp.dhis.test.e2e.helpers.QueryParamsBuilder;

/**
 * Provides events endpoints/operations associated to the parent "analytics/events".
 *
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class AnalyticsEventActions extends RestApiActions {
  public AnalyticsEventActions() {
    super("/analytics/events");
  }

  public AnalyticsEventActions(String endpoint) {
    super("/analytics/events" + endpoint);
  }

  public AnalyticsEventActions query() {
    return new AnalyticsEventActions("/query");
  }

  public AnalyticsEventActions aggregate() {
    return new AnalyticsEventActions("/aggregate");
  }

  public ApiResponse getDimensions(String programStage) {
    return this.get("/dimensions", new QueryParamsBuilder().add("programStageId", programStage))
        .validateStatus(200);
  }

  public ApiResponse getDimensions(String programStage, QueryParamsBuilder queryParamsBuilder) {
    queryParamsBuilder.add("programStageId", programStage);

    return this.get("/dimensions", queryParamsBuilder).validateStatus(200);
  }
}
