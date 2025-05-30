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
package org.hisp.dhis.test.e2e.actions.tracker;

import static org.awaitility.Awaitility.with;
import static org.hamcrest.Matchers.notNullValue;

import com.google.gson.JsonObject;
import java.io.File;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hisp.dhis.test.e2e.actions.RestApiActions;
import org.hisp.dhis.test.e2e.dto.ApiResponse;
import org.hisp.dhis.test.e2e.dto.TrackerApiResponse;
import org.hisp.dhis.test.e2e.helpers.QueryParamsBuilder;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class TrackerImportExportActions extends RestApiActions {
  private final Logger logger = LogManager.getLogger(TrackerImportExportActions.class.getName());

  public TrackerImportExportActions() {
    super("/tracker");
  }

  public ApiResponse getJob(String jobId) {
    return this.get("/jobs/" + jobId);
  }

  public void waitUntilJobIsCompleted(String jobId) {
    logger.info(String.format("Waiting until tracker job with id %s is completed", jobId));

    Callable<Boolean> jobIsCompleted = () -> getJobIsCompleted(jobId);

    with().atMost(20, TimeUnit.SECONDS).await().until(jobIsCompleted::call);

    logger.info("Tracker job is completed. Message: {}", getJob(jobId).extract("message"));
  }

  private boolean getJobIsCompleted(String jobId) {
    ApiResponse job = getJob(jobId);
    logger.info(job.getAsString());
    return job.extractList("completed").contains(true);
  }

  public TrackerApiResponse postAndGetJobReport(File file) {
    ApiResponse response = this.postFile(file);

    return getJobReportByImportResponse(response);
  }

  public TrackerApiResponse postAndGetJobReport(File file, QueryParamsBuilder queryParamsBuilder) {
    ApiResponse response = this.postFile(file, queryParamsBuilder);

    return getJobReportByImportResponse(response);
  }

  public TrackerApiResponse postAndGetJobReport(JsonObject jsonObject) {
    ApiResponse response = this.post(jsonObject);

    return getJobReportByImportResponse(response);
  }

  public TrackerApiResponse postAndGetJobReport(
      Object jsonObject, QueryParamsBuilder queryParamsBuilder) {
    ApiResponse response = this.post(jsonObject, queryParamsBuilder);

    return getJobReportByImportResponse(response);
  }

  public ApiResponse checkJobReport(String jobId, String reportMode) {
    return this.get(String.format("/jobs/%s/report?reportMode=%s", jobId, reportMode));
  }

  public TrackerApiResponse getJobReport(String jobId, String reportMode) {
    ApiResponse response =
        this.get(String.format("/jobs/%s/report?reportMode=%s", jobId, reportMode));

    // add created entities

    saveCreatedData(response);
    return new TrackerApiResponse(response);
  }

  public TrackerApiResponse getTrackedEntity(String entityId) {
    return getTrackedEntity(entityId, new QueryParamsBuilder());
  }

  public TrackerApiResponse getTrackedEntity(
      String entityId, QueryParamsBuilder queryParamsBuilder) {
    return new TrackerApiResponse(this.get("/trackedEntities/" + entityId, queryParamsBuilder));
  }

  public TrackerApiResponse getTrackedEntities(QueryParamsBuilder queryParamsBuilder) {
    return new TrackerApiResponse(this.get("/trackedEntities/", queryParamsBuilder));
  }

  public TrackerApiResponse getTrackedEntitiesCsvGZip(QueryParamsBuilder queryParamsBuilder) {
    return new TrackerApiResponse(this.get("/trackedEntities.csv.gz", queryParamsBuilder));
  }

  public TrackerApiResponse getTrackedEntitiesCsv(QueryParamsBuilder queryParamsBuilder) {
    return new TrackerApiResponse(this.get("/trackedEntities.csv", queryParamsBuilder));
  }

  public TrackerApiResponse getEnrollment(String enrollmentId) {
    return new TrackerApiResponse(this.get("/enrollments/" + enrollmentId));
  }

  public TrackerApiResponse getEnrollment(
      String enrollmentId, QueryParamsBuilder queryParamsBuilder) {
    return new TrackerApiResponse(this.get("/enrollments/" + enrollmentId, queryParamsBuilder));
  }

  public TrackerApiResponse getEnrollments(QueryParamsBuilder queryParamsBuilder) {
    return new TrackerApiResponse(this.get("/enrollments/", queryParamsBuilder));
  }

  public TrackerApiResponse getEvent(String eventId) {
    return new TrackerApiResponse(this.get("/events/" + eventId));
  }

  public TrackerApiResponse getEvents(QueryParamsBuilder queryParamsBuilder) {
    return new TrackerApiResponse(this.get("/events/", queryParamsBuilder));
  }

  public TrackerApiResponse getEventsJsonZip(QueryParamsBuilder queryParamsBuilder) {
    return new TrackerApiResponse(this.get("/events.json.zip", queryParamsBuilder));
  }

  public TrackerApiResponse getEventsJsonGZip(QueryParamsBuilder queryParamsBuilder) {
    return new TrackerApiResponse(this.get("/events.json.gz", queryParamsBuilder));
  }

  public TrackerApiResponse getEventsCsvZip(QueryParamsBuilder queryParamsBuilder) {
    return new TrackerApiResponse(this.get("/events.csv.zip", queryParamsBuilder));
  }

  public TrackerApiResponse getEventsCsvGZip(QueryParamsBuilder queryParamsBuilder) {
    return new TrackerApiResponse(this.get("/events.csv.gz", queryParamsBuilder));
  }

  public TrackerApiResponse getRelationship(String relationshipId) {
    return new TrackerApiResponse(this.get("/relationships/" + relationshipId));
  }

  public void overrideOwnership(String te, String program, String reason) {
    this.post(
            String.format(
                "/ownership/override?trackedEntity=%s&program=%s&reason=%s", te, program, reason),
            new JsonObject())
        .validateStatus(200);
  }

  public void transferOwnership(String te, String program, String ou) {
    this.update(
            String.format("/ownership/transfer?trackedEntity=%s&program=%s&ou=%s", te, program, ou),
            new JsonObject())
        .validateStatus(200);
  }

  private void saveCreatedData(ApiResponse response) {
    String[] val = {
      "TRACKED_ENTITY,/trackedEntities",
      "EVENT,/events",
      "ENROLLMENT,/enrollments",
      "RELATIONSHIP,/relationships"
    };

    for (String s : val) {
      String path =
          String.format("bundleReport.typeReportMap.%s.objectReports.uid", s.split(",")[0]);

      if (response.extractList(path) != null) {
        response.extractList(path).stream()
            .filter(Objects::nonNull)
            .forEach(id -> this.addCreatedEntity(s.split(",")[1], id.toString()));
      }
    }
  }

  private TrackerApiResponse getJobReportByImportResponse(ApiResponse response) {
    // if import is sync, just return response
    if (response.extractString("response.id") == null) {
      return new TrackerApiResponse(response);
    }

    response.validate().statusCode(200).body("response.id", notNullValue());

    String jobId = response.extractString("response.id");

    this.waitUntilJobIsCompleted(jobId);

    Callable<Integer> jobIsCompleted = () -> this.checkJobReport(jobId, "FULL").statusCode();

    with().atMost(21, TimeUnit.SECONDS).await().until(() -> jobIsCompleted.call() == 200);

    return this.getJobReport(jobId, "FULL");
  }
}
