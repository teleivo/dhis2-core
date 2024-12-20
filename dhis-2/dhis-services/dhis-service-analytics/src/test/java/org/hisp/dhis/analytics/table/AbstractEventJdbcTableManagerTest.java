/*
 * Copyright (c) 2004-2024, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
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
package org.hisp.dhis.analytics.table;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.hisp.dhis.common.ValueType;
import org.hisp.dhis.db.sql.PostgreSqlBuilder;
import org.hisp.dhis.db.sql.SqlBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AbstractEventJdbcTableManagerTest {

  @Spy private SqlBuilder sqlBuilder = new PostgreSqlBuilder();

  @InjectMocks private JdbcEventAnalyticsTableManager manager;

  @BeforeEach
  public void beforeEach() {
    manager.spatialSupport = true;
  }

  @Test
  void testGetCastExpression() {
    String expected =
        """
        case when eventdatavalues #>> '{GieVkTxp4HH, value}' ~* '^(-?[0-9]+)(\\.[0-9]+)?$' \
        then cast(eventdatavalues #>> '{GieVkTxp4HH, value}' as double precision) \
        else null end""";

    String actual =
        manager.getCastExpression(
            "eventdatavalues #>> '{GieVkTxp4HH, value}'",
            "'^(-?[0-9]+)(\\.[0-9]+)?$'",
            "double precision");

    assertEquals(expected, actual);
  }

  @Test
  void testGetSelectExpressionNumber() {
    String expected =
        """
        case when eventdatavalues #>> '{GieVkTxp4HH, value}' ~* '^(-?[0-9]+)(\\.[0-9]+)?$' \
        then cast(eventdatavalues #>> '{GieVkTxp4HH, value}' as double precision) \
        else null end""";

    String actual =
        manager.getSelectExpression(ValueType.NUMBER, "eventdatavalues #>> '{GieVkTxp4HH, value}'");

    assertEquals(expected, actual);
  }

  @Test
  void testGetSelectExpressionBoolean() {
    String expected =
        """
        case when eventdatavalues #>> '{Xl3voRRcmpo, value}' = 'true' then 1 when eventdatavalues #>> '{Xl3voRRcmpo, value}' = 'false' then 0 else null end""";

    String actual =
        manager.getSelectExpression(
            ValueType.BOOLEAN, "eventdatavalues #>> '{Xl3voRRcmpo, value}'");

    assertEquals(expected, actual);
  }

  @Test
  void testGetSelectExpressionDate() {
    String expected =
        """
        case when eventdatavalues #>> '{AL04Wbutskk, value}' ~* '^\\d{4}-\\d{2}-\\d{2}(\\s|T)?((\\d{2}:)(\\d{2}:)?(\\d{2}))?(|.(\\d{3})|.(\\d{3})Z)?$' \
        then cast(eventdatavalues #>> '{AL04Wbutskk, value}' as timestamp) \
        else null end""";

    String actual =
        manager.getSelectExpression(ValueType.DATE, "eventdatavalues #>> '{AL04Wbutskk, value}'");

    assertEquals(expected, actual);
  }

  @Test
  void testGetSelectExpressionText() {
    String expected =
        """
        eventdatavalues #>> '{FwUzmc49Pcr, value}'""";

    String actual =
        manager.getSelectExpression(ValueType.TEXT, "eventdatavalues #>> '{FwUzmc49Pcr, value}'");

    assertEquals(expected, actual);
  }

  @Test
  void testGetSelectExpressionGeometry() {
    when(manager.isSpatialSupport()).thenReturn(Boolean.TRUE);

    String expected =
        """
        ST_GeomFromGeoJSON('{"type":"Point", "coordinates":' || (eventdatavalues #>> '{C6bh7GevJfH, value}') || ', "crs":{"type":"name", "properties":{"name":"EPSG:4326"}}}')""";

    String actual =
        manager.getSelectExpression(
            ValueType.GEOJSON, "eventdatavalues #>> '{C6bh7GevJfH, value}'");

    assertEquals(expected, actual);
  }
}
