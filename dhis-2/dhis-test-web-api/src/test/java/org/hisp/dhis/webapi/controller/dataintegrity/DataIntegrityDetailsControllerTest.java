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
package org.hisp.dhis.webapi.controller.dataintegrity;

import static org.hisp.dhis.http.HttpAssertions.assertStatus;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.hisp.dhis.dataintegrity.DataIntegrityCheckType;
import org.hisp.dhis.http.HttpStatus;
import org.hisp.dhis.jsontree.JsonList;
import org.hisp.dhis.jsontree.JsonObject;
import org.hisp.dhis.test.webapi.json.domain.JsonDataIntegrityDetails;
import org.hisp.dhis.test.webapi.json.domain.JsonDataIntegrityDetails.JsonDataIntegrityIssue;
import org.hisp.dhis.test.webapi.json.domain.JsonWebMessage;
import org.hisp.dhis.webapi.controller.DataIntegrityController;
import org.junit.jupiter.api.Test;

/**
 * Tests the {@link DataIntegrityController} API with focus API returning {@link
 * org.hisp.dhis.dataintegrity.DataIntegrityDetails}.
 *
 * @author Jan Bernitt
 */
class DataIntegrityDetailsControllerTest extends AbstractDataIntegrityIntegrationTest {
  @Test
  void testLegacyChecksOnly() {
    for (DataIntegrityCheckType type : DataIntegrityCheckType.values()) {
      String check = type.getName();
      postDetails(check);
      JsonDataIntegrityDetails details = getDetails(check);
      assertTrue(details.getIssues().isEmpty());
    }
  }

  @Test
  void testSingleCheckByPath() {
    String uid =
        assertStatus(
            HttpStatus.CREATED,
            POST(
                "/categories",
                "{'name': 'CatDog', 'shortName': 'CD', 'dataDimensionType': 'ATTRIBUTE'}"));

    postDetails("categories-no-options");
    JsonDataIntegrityDetails details =
        GET("/dataIntegrity/categories-no-options/details?timeout=1000")
            .content()
            .as(JsonDataIntegrityDetails.class);

    assertTrue(details.exists());
    assertTrue(details.isObject());
    assertNotNull(details.getStartTime());
    assertNotNull(details.getCode());
    assertFalse(details.getStartTime().isAfter(details.getFinishedTime()));
    JsonList<JsonDataIntegrityIssue> issues = details.getIssues();
    assertTrue(issues.exists());
    assertEquals(1, issues.size());
    assertEquals(uid, issues.get(0).getId());
    assertEquals("CatDog", issues.get(0).getName());
    assertEquals("categories", details.getIssuesIdType());
  }

  @Test
  void testCompletedChecks() {
    String uid =
        assertStatus(
            HttpStatus.CREATED,
            POST(
                "/categories",
                "{'name': 'CatDog', 'shortName': 'CD', 'dataDimensionType': 'ATTRIBUTE'}"));

    postDetails("categories-no-options");
    JsonDataIntegrityDetails details =
        GET("/dataIntegrity/categories-no-options/details?timeout=1000")
            .content()
            .as(JsonDataIntegrityDetails.class);
    assertNotNull(details);

    // OBS! The result is based on application scoped map so there might be other values from other
    // tests
    assertTrue(
        GET("/dataIntegrity/details/completed")
            .content()
            .stringValues()
            .contains("categories_no_options"));
  }

  @Test
  void testRunDetailsCheck_WithBody() {
    JsonObject trigger = POST("/dataIntegrity/details", "['DE']").content(); // datasets_empty
    assertTrue(trigger.isA(JsonWebMessage.class));

    // wait for check to complete
    JsonDataIntegrityDetails details =
        GET("/dataIntegrity/DE/details?timeout=1000").content().as(JsonDataIntegrityDetails.class);
    assertTrue(details.isObject());

    assertTrue(
        GET("/dataIntegrity/details/completed")
            .content()
            .stringValues()
            .contains("datasets_empty"));
  }
}
