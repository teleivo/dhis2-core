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

import static org.hisp.dhis.test.webapi.Assertions.assertWebMessage;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.hisp.dhis.http.HttpStatus;
import org.hisp.dhis.jsontree.JsonObject;
import org.hisp.dhis.sms.incoming.IncomingSms;
import org.hisp.dhis.sms.incoming.IncomingSmsService;
import org.hisp.dhis.test.webapi.H2ControllerIntegrationTestBase;
import org.hisp.dhis.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests the {@link org.hisp.dhis.webapi.controller.sms.SmsInboundController} using (mocked) REST
 * requests.
 *
 * @author Jan Bernitt
 */
@Transactional
class SmsInboundControllerTest extends H2ControllerIntegrationTestBase {

  @Autowired private IncomingSmsService incomingSMSService;

  @Test
  void testGetInboundSMSMessage() {
    JsonObject list = GET("/sms/inbound").content();
    assertEquals(0, list.getArray("inboundsmss").size());
  }

  @Test
  void testGetInboundSMSMessage_Forbidden() {
    User guestUser = createUserWithAuth("guestuser", "NONE");
    injectSecurityContextUser(guestUser);

    assertWebMessage(
        "Forbidden",
        403,
        "ERROR",
        "Access is denied, requires one Authority from [F_MOBILE_SENDSMS]",
        GET("/sms/inbound").content(HttpStatus.FORBIDDEN));
  }

  @Test
  void testReceiveSMSMessage() {
    assertWebMessage(
        "Conflict",
        409,
        "ERROR",
        "Originator's number does not match user's Phone number",
        POST("/sms/inbound?originator=me&message=text").content(HttpStatus.CONFLICT));
  }

  @Test
  void testReceiveSMSMessage_NoOriginator() {
    assertWebMessage(
        "Conflict",
        409,
        "ERROR",
        "Originator must be specified",
        POST("/sms/inbound?originator=&message=text").content(HttpStatus.CONFLICT));
  }

  @Test
  void testReceiveSMSMessage_NoMessage() {
    assertWebMessage(
        "Conflict",
        409,
        "ERROR",
        "Message must be specified",
        POST("/sms/inbound?originator=me&message=").content(HttpStatus.CONFLICT));
  }

  @Test
  void testReceiveSMSMessageWithBody() {
    assertWebMessage(
        "Conflict",
        409,
        "ERROR",
        "Originator's number does not match user's Phone number",
        POST("/sms/inbound", "{'originator':'me','text':'text'}").content(HttpStatus.CONFLICT));
  }

  @Test
  void testImportUnparsedSMSMessages() {
    assertWebMessage(
        "OK", 200, "OK", "Import successful", POST("/sms/inbound/import").content(HttpStatus.OK));
  }

  @Test
  void testDeleteInboundMessage() {
    IncomingSms sms = new IncomingSms();
    sms.setOriginator("me");
    sms.setText("text");
    incomingSMSService.save(sms);
    assertWebMessage(
        "OK",
        200,
        "OK",
        "IncomingSms with " + sms.getUid() + " deleted",
        DELETE("/sms/inbound/" + sms.getUid()).content(HttpStatus.OK));
  }

  @Test
  void testDeleteInboundMessage_NoSuchObject() {
    assertWebMessage(
        "Not Found",
        404,
        "ERROR",
        "No IncomingSms with id 'xyz' was found.",
        DELETE("/sms/inbound/xyz").content(HttpStatus.NOT_FOUND));
  }

  @Test
  void testDeleteInboundMessages() {
    IncomingSms sms = new IncomingSms();
    sms.setOriginator("me");
    sms.setText("text");
    incomingSMSService.save(sms);
    assertWebMessage(
        "OK",
        200,
        "OK",
        "Objects deleted",
        DELETE("/sms/inbound?ids=" + sms.getUid()).content(HttpStatus.OK));
  }
}
