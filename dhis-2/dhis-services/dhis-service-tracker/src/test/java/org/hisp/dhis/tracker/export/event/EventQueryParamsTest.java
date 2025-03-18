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
package org.hisp.dhis.tracker.export.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.hisp.dhis.common.QueryFilter;
import org.hisp.dhis.common.QueryOperator;
import org.hisp.dhis.common.SortDirection;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.test.TestBase;
import org.hisp.dhis.trackedentity.TrackedEntityAttribute;
import org.hisp.dhis.tracker.export.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EventQueryParamsTest extends TestBase {

  private TrackedEntityAttribute tea1;

  private DataElement de1;

  @BeforeEach
  void setUp() {
    tea1 = createTrackedEntityAttribute('a');
    de1 = createDataElement('a');
  }

  @Test
  void shouldKeepExistingAttributeFiltersWhenOrderingByAttribute() {
    EventQueryParams params = new EventQueryParams();

    QueryFilter filter = new QueryFilter(QueryOperator.EQ, "summer day");
    params.filterBy(tea1, filter);

    assertEquals(Map.of(tea1, List.of(filter)), params.getAttributes());

    params.orderBy(tea1, SortDirection.DESC);

    assertEquals(Map.of(tea1, List.of(filter)), params.getAttributes());
  }

  @Test
  void shouldAddDataElementToOrderButNotToDataElementsWhenOrderingByDataElement() {
    EventQueryParams params = new EventQueryParams();

    params.orderBy(de1, SortDirection.ASC);

    assertEquals(List.of(new Order(de1, SortDirection.ASC)), params.getOrder());
    assertTrue(params.getDataElements().isEmpty());
    assertFalse(params.hasDataElementFilter());
  }

  @Test
  void shouldKeepExistingDataElementFiltersWhenOrderingByDataElement() {
    EventQueryParams params = new EventQueryParams();

    QueryFilter filter = new QueryFilter(QueryOperator.EQ, "summer day");
    params.filterBy(de1, filter);

    assertTrue(params.hasDataElementFilter());
    assertEquals(Map.of(de1, List.of(filter)), params.getDataElements());

    params.orderBy(de1, SortDirection.ASC);

    assertTrue(params.hasDataElementFilter());
    assertEquals(Map.of(de1, List.of(filter)), params.getDataElements());
  }
}
