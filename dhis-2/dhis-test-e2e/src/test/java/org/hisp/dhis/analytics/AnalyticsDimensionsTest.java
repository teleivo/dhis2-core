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
package org.hisp.dhis.analytics;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.in;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.oneOf;
import static org.hamcrest.Matchers.startsWith;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.hisp.dhis.ApiTest;
import org.hisp.dhis.helpers.matchers.CustomMatchers;
import org.hisp.dhis.helpers.matchers.Sorted;
import org.hisp.dhis.test.e2e.Constants;
import org.hisp.dhis.test.e2e.actions.analytics.AnalyticsEnrollmentsActions;
import org.hisp.dhis.test.e2e.actions.analytics.AnalyticsEventActions;
import org.hisp.dhis.test.e2e.actions.analytics.AnalyticsTrackedEntityActions;
import org.hisp.dhis.test.e2e.actions.metadata.ProgramActions;
import org.hisp.dhis.test.e2e.actions.metadata.TrackedEntityAttributeActions;
import org.hisp.dhis.test.e2e.dto.ApiResponse;
import org.hisp.dhis.test.e2e.dto.Program;
import org.hisp.dhis.test.e2e.helpers.QueryParamsBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
@Tag("category:analytics")
public class AnalyticsDimensionsTest extends ApiTest {
  private Program trackerProgram = Constants.TRACKER_PROGRAM;

  private AnalyticsEnrollmentsActions analyticsEnrollmentsActions;

  private AnalyticsEventActions analyticsEventActions;

  private AnalyticsTrackedEntityActions analyticsTrackedEntityActions;

  private TrackedEntityAttributeActions trackedEntityAttributeActions;

  private ProgramActions programActions;

  @BeforeAll
  public void beforeAll() {
    trackedEntityAttributeActions = new TrackedEntityAttributeActions();
    programActions = new ProgramActions();
    analyticsEnrollmentsActions = new AnalyticsEnrollmentsActions();
    analyticsEventActions = new AnalyticsEventActions();
    analyticsTrackedEntityActions = new AnalyticsTrackedEntityActions();
  }

  Stream<Arguments> shouldOrder() {
    return Stream.of(
        Arguments.of("name", "desc"),
        Arguments.of("code", "desc"),
        Arguments.of("uid", "asc"),
        Arguments.of("id", "asc"),
        Arguments.of("lastUpdated", "desc"),
        Arguments.of("created", "asc"),
        Arguments.of("displayName", "desc"),
        Arguments.of("displayName", "asc"),
        Arguments.of("dimensionType", "desc"));
  }

  @MethodSource
  @ParameterizedTest
  public void shouldOrder(String property, String direction) {
    QueryParamsBuilder queryParamsBuilder =
        new QueryParamsBuilder().add("order", String.format("%s:%s", property, direction));

    analyticsEnrollmentsActions
        .query()
        .getDimensions(trackerProgram.getUid(), queryParamsBuilder)
        .validate()
        .body("dimensions", hasSize(greaterThanOrEqualTo(1)))
        .body("dimensions." + property, Sorted.by(direction));

    analyticsEventActions
        .query()
        .getDimensions(trackerProgram.getProgramStages().get(0), queryParamsBuilder)
        .validate()
        .body("dimensions", hasSize(greaterThanOrEqualTo(1)))
        .body("dimensions." + property, Sorted.by(direction));

    analyticsTrackedEntityActions
        .query()
        .getDimensions(trackerProgram.getTrackedEntityType(), queryParamsBuilder)
        .validate()
        .body("dimensions", hasSize(greaterThanOrEqualTo(1)))
        .body("dimensions." + property, Sorted.by(direction));
  }

  @Test
  public void shouldReturnDataElementsFromAllStages() {
    analyticsEnrollmentsActions
        .query()
        .getDimensionsByDimensionType(trackerProgram.getUid(), "DATA_ELEMENT")
        .validate()
        .body(
            "dimensions.id",
            everyItem(CustomMatchers.startsWithOneOf(trackerProgram.getProgramStages())));
  }

  @Test
  public void shouldOnlyReturnProgramTrackedEntityAttributes() {
    String teaNotAssignedToProgram = trackedEntityAttributeActions.create("TEXT");

    analyticsEnrollmentsActions
        .query()
        .getDimensionsByDimensionType(trackerProgram.getUid(), "PROGRAM_ATTRIBUTE")
        .validate()
        .body("dimensions.uid", not(hasItem(equalTo(teaNotAssignedToProgram))));

    analyticsEnrollmentsActions
        .aggregate()
        .getDimensionsByDimensionType(trackerProgram.getUid(), "PROGRAM_ATTRIBUTE")
        .validate()
        .body("dimensions.uid", not(hasItem(equalTo(teaNotAssignedToProgram))));

    analyticsEventActions
        .aggregate()
        .getDimensions(trackerProgram.getProgramStages().get(0))
        .validate()
        .body("dimensions.uid", not(hasItem(equalTo(teaNotAssignedToProgram))));
  }

  @Test
  public void shouldOnlyReturnConfidentialAttributeInAggregateDimensions() {
    String confidentialAttribute = trackedEntityAttributeActions.create("NUMBER", true, true);
    programActions
        .addAttribute(Constants.TRACKER_PROGRAM_ID, confidentialAttribute, false)
        .validateStatus(200);

    analyticsEnrollmentsActions
        .query()
        .getDimensionsByDimensionType(trackerProgram.getUid(), "PROGRAM_ATTRIBUTE")
        .validate()
        .body("dimensions.uid", not(CoreMatchers.hasItem(confidentialAttribute)));

    analyticsEnrollmentsActions
        .aggregate()
        .getDimensionsByDimensionType(trackerProgram.getUid(), "PROGRAM_ATTRIBUTE")
        .validate()
        .body("dimensions.uid", CoreMatchers.hasItem(confidentialAttribute));
  }

  @ValueSource(strings = {"DATA_ELEMENT", "PROGRAM_ATTRIBUTE"})
  @ParameterizedTest
  public void shouldLimitAggregateDimensionsByValueTypes(String dimensionType) {
    List<String> acceptedValueTypes =
        Arrays.asList(
            "NUMBER",
            "UNIT_INTERVAL",
            "PERCENTAGE",
            "INTEGER",
            "INTEGER_POSITIVE",
            "INTEGER_NEGATIVE",
            "INTEGER_ZERO_OR_POSITIVE",
            "BOOLEAN",
            "TRUE_ONLY");

    Consumer<ApiResponse> validate =
        response -> {
          response
              .validate()
              .body("dimensions", hasSize(greaterThanOrEqualTo(1)))
              .body("dimensions.valueType", Matchers.everyItem(in(acceptedValueTypes)));
        };

    validate.accept(
        analyticsEnrollmentsActions
            .aggregate()
            .getDimensionsByDimensionType(trackerProgram.getUid(), dimensionType));
    validate.accept(
        analyticsEventActions.aggregate().getDimensions(trackerProgram.getProgramStages().get(0)));
  }

  Stream<Arguments> shouldFilter() {
    return Stream.of(
        Arguments.of("uid", "eq", "ISTEJWQz7tr", equalTo("ISTEJWQz7tr")),
        Arguments.of("uid", "ieq", "isteJWQz7tr", containsString("ISTEJWQz7tr")),
        Arguments.of("id", "ne", "ISTEJWQz7tr", not(equalTo("ISTEJWQz7tr"))),
        Arguments.of("code", "like", "TA", containsString("TA")),
        Arguments.of("valueType", "like", "TEXT", oneOf("TEXT", "LONG_TEXT")),
        Arguments.of(
            "id",
            "startsWith",
            trackerProgram.getProgramStages().get(0),
            startsWith(trackerProgram.getProgramStages().get(0))),
        Arguments.of("id", "endsWith", "BuZ5LGNfGET", endsWith("BuZ5LGNfGET")),
        Arguments.of(
            "id",
            "!startsWith",
            trackerProgram.getProgramStages().get(0),
            not(startsWith(trackerProgram.getProgramStages().get(0)))),
        Arguments.of("dimensionType", "eq", "DATA_ELEMENT", equalTo("DATA_ELEMENT")),
        Arguments.of("dimensionType", "eq", "PROGRAM_INDICATOR", equalTo("PROGRAM_INDICATOR")),
        Arguments.of("dimensionType", "eq", "PROGRAM_ATTRIBUTE", equalTo("PROGRAM_ATTRIBUTE")));
  }

  @ParameterizedTest
  @MethodSource
  public void shouldFilter(String property, String operator, String value, Matcher matcher) {
    Consumer<ApiResponse> validate =
        response -> {
          response
              .validate()
              .statusCode(200)
              .body("dimensions", hasSize(greaterThanOrEqualTo(1)))
              .body("dimensions." + property, everyItem(matcher));
        };

    validate.accept(
        analyticsEnrollmentsActions
            .query()
            .getDimensions(
                trackerProgram.getUid(),
                new QueryParamsBuilder()
                    .add(String.format("filter=%s:%s:%s", property, operator, value))));

    validate.accept(
        analyticsEventActions
            .query()
            .getDimensions(
                trackerProgram.getProgramStages().get(0),
                new QueryParamsBuilder()
                    .add(String.format("filter=%s:%s:%s", property, operator, value))));
  }

  @Test
  public void shouldReturnAllProgramAttributes() {
    List<String> programAttributes =
        programActions
            .get(
                new QueryParamsBuilder()
                    .add("fields", "*")
                    .add("filter", "trackedEntityType.id:eq:" + Constants.TRACKED_ENTITY_TYPE))
            .extractList(
                "programs.programTrackedEntityAttributes.flatten().trackedEntityAttribute.id",
                String.class)
            .stream()
            .distinct()
            .collect(Collectors.toList());

    analyticsTrackedEntityActions
        .query()
        .getDimensions(
            Constants.TRACKED_ENTITY_TYPE,
            new QueryParamsBuilder().add("filter", "dimensionType:endsWith:_ATTRIBUTE"))
        .validate()
        .statusCode(200)
        .body("dimensions", hasSize(equalTo(programAttributes.size())))
        .body("dimensions.uid", everyItem(in(programAttributes)));
  }

  @Test
  public void shouldReturnAllDataElements() {
    List<String> dataElements =
        programActions
            .get(
                new QueryParamsBuilder()
                    .add("filter", "trackedEntityType.id:eq:" + Constants.TRACKED_ENTITY_TYPE)
                    .add("fields", "programStages[programStageDataElements"))
            .extractList(
                "programs.programStages.programStageDataElements.flatten().dataElement.id");

    Set<String> distinctDataElements = new HashSet<>(dataElements);

    analyticsTrackedEntityActions
        .query()
        .getDimensions(
            Constants.TRACKED_ENTITY_TYPE,
            new QueryParamsBuilder().add("filter", "dimensionType:eq:DATA_ELEMENT"))
        .validate()
        .statusCode(200)
        .body("dimensions", hasSize(equalTo(dataElements.size())))
        .body("dimensions.uid", everyItem(in(distinctDataElements)));
  }

  @Test
  void ProgramIndicatorsShouldHavePrefix() {
    analyticsTrackedEntityActions
        .query()
        .getDimensions(
            Constants.TRACKED_ENTITY_TYPE,
            new QueryParamsBuilder().add("filter", "dimensionType:eq:PROGRAM_INDICATOR"))
        .validate()
        .statusCode(200)
        .body("dimensions", hasSize(greaterThanOrEqualTo(1)))
        .body("dimensions.id", everyItem(containsExactlyOne('.')));
  }

  public static TypeSafeDiagnosingMatcher<String> containsExactlyOne(Character character) {
    return new TypeSafeDiagnosingMatcher<>() {
      @Override
      protected boolean matchesSafely(String item, Description mismatchDescription) {
        if (item != null) {
          return item.chars().filter(ch -> ch == character).count() == 1;
        }
        return false;
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("a string that contains exactly one " + character);
      }
    };
  }
}
