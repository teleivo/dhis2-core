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
package org.hisp.dhis.tracker.imports;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import com.google.gson.JsonObject;
import java.io.File;
import java.util.function.Consumer;
import org.hamcrest.Matchers;
import org.hisp.dhis.helpers.file.FileReaderUtils;
import org.hisp.dhis.test.e2e.Constants;
import org.hisp.dhis.test.e2e.dto.ApiResponse;
import org.hisp.dhis.test.e2e.dto.TrackerApiResponse;
import org.hisp.dhis.test.e2e.helpers.JsonObjectBuilder;
import org.hisp.dhis.test.e2e.helpers.QueryParamsBuilder;
import org.hisp.dhis.tracker.TrackerApiTest;
import org.hisp.dhis.tracker.imports.databuilder.TrackedEntityDataBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class ImportStrategyTests extends TrackerApiTest {
  @BeforeAll
  public void beforeAll() {
    loginActions.loginAsSuperUser();
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "src/test/resources/tracker/importer/trackedEntities/trackedEntitiesWithEnrollmentsAndEvents.json",
        "src/test/resources/tracker/importer/trackedEntities/trackedEntityAndEnrollment.json",
        "src/test/resources/tracker/importer/trackedEntities/trackedEntities.json",
        "src/test/resources/tracker/importer/events/events.json"
      })
  public void shouldDeleteWithDeleteStrategy(String fileName) throws Exception {
    // arrange
    JsonObject teBody = new FileReaderUtils().readJsonAndGenerateData(new File(fileName));

    trackerImportExportActions.postAndGetJobReport(teBody).validateSuccessfulImport();

    // act
    ApiResponse response =
        trackerImportExportActions.postAndGetJobReport(
            teBody, new QueryParamsBuilder().add("importStrategy=DELETE"));

    // assert
    response
        .validate()
        .statusCode(200)
        .body("status", equalTo("OK"))
        .body("stats.deleted", Matchers.greaterThanOrEqualTo(1));
  }

  @Test
  public void shouldDeleteReferencingDataWhenTrackedEntityIsDeleted() throws Exception {
    // arrange
    JsonObject body =
        new FileReaderUtils()
            .readJsonAndGenerateData(
                new File(
                    "src/test/resources/tracker/importer/trackedEntities/trackedEntityAndEnrollment.json"));

    TrackerApiResponse response =
        trackerImportExportActions.postAndGetJobReport(body).validateSuccessfulImport();
    String teId = response.extractImportedTrackedEntities().get(0);
    String enrollmentId = response.extractImportedEnrollments().get(0);

    body.remove("enrollments");

    // act
    response =
        trackerImportExportActions
            .postAndGetJobReport(body, new QueryParamsBuilder().add("importStrategy=DELETE"))
            .validateSuccessfulImport();

    // assert
    response.validateSuccessfulImport().validate().body("stats.deleted", Matchers.equalTo(1));

    trackerImportExportActions.getTrackedEntity(teId).validate().statusCode(404);
    trackerImportExportActions.get("/enrollments/" + enrollmentId).validate().statusCode(404);
  }

  @Test
  public void shouldDeleteReferencingEventsWhenEnrollmentIsDeleted() {
    // arrange
    JsonObject body =
        new TrackedEntityDataBuilder()
            .buildWithEnrollmentAndEvent(
                Constants.TRACKED_ENTITY_TYPE,
                Constants.ORG_UNIT_IDS[0],
                Constants.TRACKER_PROGRAM_ID,
                "PaOOjwLVW23");

    TrackerApiResponse response =
        trackerImportExportActions.postAndGetJobReport(body).validateSuccessfulImport();
    String teId = response.extractImportedTrackedEntities().get(0);
    String enrollmentId = response.extractImportedEnrollments().get(0);
    String eventId1 = response.extractImportedEvents().get(0);

    body =
        trackerImportExportActions
            .getEnrollment(enrollmentId)
            .validateStatus(200)
            .getBodyAsJsonBuilder()
            .wrapIntoArray("enrollments");

    // act
    response =
        trackerImportExportActions
            .postAndGetJobReport(body, new QueryParamsBuilder().add("importStrategy=DELETE"))
            .validateSuccessfulImport();

    // assert
    response.validateSuccessfulImport().validate().body("stats.deleted", Matchers.equalTo(1));

    trackerImportExportActions
        .getTrackedEntity(teId + "?fields=*")
        .validate()
        .statusCode(200)
        .body("enrollments", hasSize(0));

    trackerImportExportActions.get("/enrollments/" + enrollmentId).validate().statusCode(404);
    trackerImportExportActions.get("/events/" + eventId1).validate().statusCode(404);
  }

  @Test
  public void shouldDeleteWithOnlyIdInThePayload() throws Exception {
    TrackerApiResponse response = super.importTrackedEntitiesWithEnrollmentAndEvent();

    String eventId = response.extractImportedEvents().get(0);
    String enrollmentId = response.extractImportedEnrollments().get(0);
    String teId = response.extractImportedTrackedEntities().get(0);

    Consumer<JsonObject> deleteAndValidate =
        (payload) -> {
          trackerImportExportActions
              .postAndGetJobReport(payload, new QueryParamsBuilder().add("importStrategy=DELETE"))
              .validateSuccessfulImport()
              .validate()
              .body("stats.deleted", Matchers.equalTo(1));
        };

    deleteAndValidate.accept(
        new JsonObjectBuilder().addProperty("event", eventId).wrapIntoArray("events"));
    deleteAndValidate.accept(
        new JsonObjectBuilder()
            .addProperty("enrollment", enrollmentId)
            .wrapIntoArray("enrollments"));
    deleteAndValidate.accept(
        new JsonObjectBuilder()
            .addProperty("trackedEntity", teId)
            .wrapIntoArray("trackedEntities"));
  }
}
