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
package org.hisp.dhis.analytics.orgunit;

import java.util.Map;
import org.hisp.dhis.common.Grid;
import org.hisp.dhis.common.IllegalQueryException;

public interface OrgUnitAnalyticsService {
  /**
   * Returns parameters for the given query.
   *
   * @param orgUnits the organisation unit string.
   * @param orgUnitGroupSets the organisation unit group set string.
   * @param columns the organisation unit group set to place as columns, implies rendering in table
   *     layout, can be null.
   * @return a {@link OrgUnitQueryParams}.
   */
  OrgUnitQueryParams getParams(String orgUnits, String orgUnitGroupSets, String columns);

  /**
   * Returns the org unit data for the given parameters.
   *
   * @param params the {@link OrgUnitQueryParams}.
   * @return a {@link Grid}.
   */
  Grid getOrgUnitData(OrgUnitQueryParams params);

  /**
   * Returns the org unit data as a map with the metadata as key and org unit count as value for the
   * given parameters.
   *
   * @param params the {@link OrgUnitQueryParams}.
   * @return a {@link Map}.
   */
  Map<String, Object> getOrgUnitDataMap(OrgUnitQueryParams params);

  /**
   * Validates the given parameters. Throws an {@link IllegalQueryException} if invalid.
   *
   * @param params the {@link OrgUnitQueryParams}.
   * @throws IllegalQueryException if invalid.
   */
  void validate(OrgUnitQueryParams params) throws IllegalQueryException;
}
