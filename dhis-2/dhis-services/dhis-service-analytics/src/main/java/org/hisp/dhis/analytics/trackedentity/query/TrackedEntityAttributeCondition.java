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
package org.hisp.dhis.analytics.trackedentity.query;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.hisp.dhis.analytics.common.ValueTypeMapping;
import org.hisp.dhis.analytics.common.params.dimension.DimensionIdentifier;
import org.hisp.dhis.analytics.common.params.dimension.DimensionParam;
import org.hisp.dhis.analytics.common.params.dimension.DimensionParamItem;
import org.hisp.dhis.analytics.common.query.AndCondition;
import org.hisp.dhis.analytics.common.query.BaseRenderable;
import org.hisp.dhis.analytics.common.query.BinaryConditionRenderer;
import org.hisp.dhis.analytics.common.query.Field;
import org.hisp.dhis.analytics.trackedentity.query.context.sql.QueryContext;

@RequiredArgsConstructor(staticName = "of")
public class TrackedEntityAttributeCondition extends BaseRenderable {
  private final DimensionIdentifier<DimensionParam> dimensionIdentifier;

  private final QueryContext queryContext;

  @Override
  public String render() {
    List<BinaryConditionRenderer> renderers = new ArrayList<>();

    ValueTypeMapping valueTypeMapping =
        ValueTypeMapping.fromValueType(dimensionIdentifier.getDimension().getValueType());

    for (DimensionParamItem item : dimensionIdentifier.getDimension().getItems()) {
      BinaryConditionRenderer binaryConditionRenderer =
          BinaryConditionRenderer.of(
              Field.of(dimensionIdentifier.getDimension().getUid()),
              item.getOperator(),
              item.getValues(),
              valueTypeMapping,
              queryContext);

      renderers.add(binaryConditionRenderer);
    }

    return AndCondition.of(renderers).render();
  }
}
