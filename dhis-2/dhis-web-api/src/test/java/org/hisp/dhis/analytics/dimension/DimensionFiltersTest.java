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
package org.hisp.dhis.analytics.dimension;

import static org.apache.commons.lang3.tuple.Pair.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hisp.dhis.webapi.dimension.DimensionFilters.EMPTY_DATA_DIMENSION_FILTER;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.Pair;
import org.hisp.dhis.webapi.dimension.DimensionFilters;
import org.hisp.dhis.webapi.dimension.DimensionResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DimensionFiltersTest {

  /**
   * A map of all the fields in {@link DimensionResponse} and a function that creates a {@link
   * DimensionResponse} with that field set to the given value.
   */
  private static final Map<String, Function<String, DimensionResponse>> BUILDER_MAP =
      Map.of(
          "id", s -> DimensionResponse.builder().id(s).build(),
          "uid", s -> DimensionResponse.builder().uid(s).build(),
          "code", s -> DimensionResponse.builder().code(s).build(),
          "valueType", s -> DimensionResponse.builder().valueType(s).build(),
          "name", s -> DimensionResponse.builder().name(s).build(),
          "dimensionType", s -> DimensionResponse.builder().dimensionType(s).build(),
          "displayName", s -> DimensionResponse.builder().displayName(s).build(),
          "displayShortName", s -> DimensionResponse.builder().displayShortName(s).build());

  public static final String STRING_TO_TEST_FILTER_ON = "TeSt";

  /**
   * A map of all the operators and the expected result of applying them to the {@link
   * DimensionResponse} created by the corresponding {@link Function} in {@link #BUILDER_MAP} with
   * the given {@link #STRING_TO_TEST_FILTER_ON}. The left value is the value to test for positive
   * assertion, the right value is the value to test for negative.
   */
  private static final Map<String, Pair<String, String>> OPERATORS =
      Stream.concat(
              Map.of(
                  "startsWith", of("Te", "eS"),
                  "!startsWith", of("eS", "Te"),
                  "endsWith", of("St", "eS"),
                  "!endsWith", of("eS", "St"),
                  "eq", of("TeSt", "tEsT"),
                  "!eq", of("whatever", "TeSt"),
                  "ieq", of("test", "random"))
                  .entrySet()
                  .stream(),
              Map.of(
                  "ne", of("random", "TeSt"),
                  "like", of("eS", "es"),
                  "!like", of("es", "eS"),
                  "ilike", of("es", "et"),
                  "!ilike", of("et", "es"))
                  .entrySet()
                  .stream())
          .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

  @Test
  void testDimensionFilterConstructor() {

    DimensionFilters dimensionFilters =
        DimensionFilters.of(
            List.of(
                "unsupportedField:eq:1",
                "name:unsupportedOperator:3",
                "failingToParseFilter",
                "name:eq:test"));

    assertThat(dimensionFilters.getFilters(), hasSize(1));

    DimensionFilters anotherDimensionFilters =
        DimensionFilters.of(
            List.of(
                "unsupportedField:eq:1", "name:unsupportedOperator:3", "failingToParseFilter;"));

    assertNull(anotherDimensionFilters.getFilters());
    assertThat(anotherDimensionFilters, is(EMPTY_DATA_DIMENSION_FILTER));
  }

  @Test
  void testFields() {
    DimensionFilters dimensionFilters =
        DimensionFilters.of(
            BUILDER_MAP.keySet().stream().map(s -> s + ":eq:1").collect(Collectors.toList()));

    assertThat(dimensionFilters.getFilters(), hasSize(BUILDER_MAP.size()));
  }

  @Test
  void testOperators() {
    String aFieldName = BUILDER_MAP.keySet().stream().findFirst().orElse("");

    DimensionFilters dimensionFilters =
        DimensionFilters.of(
            OPERATORS.keySet().stream()
                .map(operator -> String.join(":", aFieldName, operator, "1"))
                .collect(Collectors.toList()));

    assertThat(dimensionFilters.getFilters(), hasSize(OPERATORS.size()));
  }

  @Test
  void testFieldsAndOperators() {

    BUILDER_MAP.forEach(
        (s, builder) -> assertAllOpsOnField(s, builder.apply(STRING_TO_TEST_FILTER_ON)));
  }

  private void assertAllOpsOnField(String fieldName, DimensionResponse response) {
    OPERATORS.forEach((op, pair) -> assertFieldOnOp(fieldName, op, pair, response));
  }

  private void assertFieldOnOp(
      String fieldName, String op, Pair<String, String> pair, DimensionResponse response) {
    String whenTrue = pair.getLeft();
    String whenFalse = pair.getRight();

    DimensionFilters whenTrueFilter =
        DimensionFilters.of(Collections.singleton(String.join(":", fieldName, op, whenTrue)));

    DimensionFilters whenFalseFilter =
        DimensionFilters.of(Collections.singleton(String.join(":", fieldName, op, whenFalse)));

    Assertions.assertTrue(whenTrueFilter.test(response));
    Assertions.assertFalse(whenFalseFilter.test(response));
  }
}
