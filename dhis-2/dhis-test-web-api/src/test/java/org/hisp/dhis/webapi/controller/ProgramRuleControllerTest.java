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
package org.hisp.dhis.webapi.controller;

import static org.hisp.dhis.http.HttpAssertions.assertStatus;
import static org.hisp.dhis.test.webapi.Assertions.assertWebMessage;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.hisp.dhis.feedback.ErrorCode;
import org.hisp.dhis.http.HttpStatus;
import org.hisp.dhis.program.Program;
import org.hisp.dhis.test.webapi.H2ControllerIntegrationTestBase;
import org.hisp.dhis.test.webapi.json.domain.JsonErrorReport;
import org.hisp.dhis.test.webapi.json.domain.JsonImportSummary;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests the {@link org.hisp.dhis.webapi.controller.event.ProgramRuleController} using (mocked) REST
 * requests.
 *
 * @author Jan Bernitt
 */
@Transactional
class ProgramRuleControllerTest extends H2ControllerIntegrationTestBase {

  @Test
  void testValidateCondition() {
    String pId =
        assertStatus(
            HttpStatus.CREATED,
            POST(
                "/programs/",
                "{'name':'P1', 'shortName':'P1', 'programType':'WITHOUT_REGISTRATION'}"));
    assertWebMessage(
        "OK",
        200,
        "OK",
        "Valid",
        POST("/programRules/condition/description?programId=" + pId, "1 != 1")
            .content(HttpStatus.OK));
  }

  @Test
  void testValidateCondition_NoSuchProgram() {
    assertEquals(
        "Program is specified but does not exist: abcdefghijk",
        POST("/programRules/condition/description?programId=abcdefghijk", "1 != 1")
            .error(HttpStatus.BAD_REQUEST)
            .getMessage());
  }

  @Test
  void testDuplicateNameInProgram() {
    Program program = createProgram('A');
    manager.save(program);
    assertStatus(HttpStatus.OK, GET("/programs/{id}", program.getUid()));
    assertStatus(
        HttpStatus.OK,
        POST(
            "/metadata/",
            "{'programRules':[{'name':'test', 'program':{ 'id':'" + program.getUid() + "'}}]}"));

    JsonImportSummary response =
        POST(
                "/metadata/",
                "{'programRules':[{'name':'test', 'program':{ 'id':'" + program.getUid() + "'}}]}")
            .content(HttpStatus.CONFLICT)
            .get("response")
            .as(JsonImportSummary.class);
    assertEquals(
        "The Program Rule name test already exist in Program ProgramA",
        response
            .find(JsonErrorReport.class, error -> error.getErrorCode() == ErrorCode.E4057)
            .getMessage());
  }
}
