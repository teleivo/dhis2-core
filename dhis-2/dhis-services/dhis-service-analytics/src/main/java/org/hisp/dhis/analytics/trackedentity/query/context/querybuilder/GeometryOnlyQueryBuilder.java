/*
 * Copyright (c) 2004-2023, University of Oslo
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
package org.hisp.dhis.analytics.trackedentity.query.context.querybuilder;

import static org.hisp.dhis.analytics.common.query.GroupableCondition.ofUngroupedCondition;

import java.util.List;
import java.util.stream.Stream;
import org.hisp.dhis.analytics.common.params.dimension.DimensionIdentifier;
import org.hisp.dhis.analytics.common.params.dimension.DimensionParam;
import org.hisp.dhis.analytics.common.query.GroupableCondition;
import org.hisp.dhis.analytics.trackedentity.query.GeometryOnlyCondition;
import org.hisp.dhis.analytics.trackedentity.query.context.sql.QueryContext;
import org.hisp.dhis.analytics.trackedentity.query.context.sql.SqlQueryBuilderAdaptor;
import org.springframework.stereotype.Service;

/**
 * This class is responsible for adding the where clause for the geometry only query. This happens
 * we want to return only TE with geometry.
 */
@Service
public class GeometryOnlyQueryBuilder extends SqlQueryBuilderAdaptor {
  @Override
  protected Stream<GroupableCondition> getWhereClauses(
      QueryContext queryContext, List<DimensionIdentifier<DimensionParam>> unused) {
    if (queryContext.getContextParams().getCommonRaw().isGeometryOnly()) {
      return Stream.of(ofUngroupedCondition(GeometryOnlyCondition.INSTANCE));
    }
    return Stream.empty();
  }

  @Override
  public boolean alwaysRun() {
    return true;
  }
}
