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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.hisp.dhis.attribute.Attribute.ObjectType;
import org.hisp.dhis.http.HttpStatus;
import org.hisp.dhis.jsontree.JsonObject;
import org.hisp.dhis.test.webapi.PostgresControllerIntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests the {@link AttributeController}.
 *
 * @author Jan Bernitt
 */
@Transactional
class AttributeControllerTest extends PostgresControllerIntegrationTestBase {

  @Test
  void testGistList() {
    String uid =
        assertStatus(
            HttpStatus.CREATED,
            POST(
                "/attributes",
                "{"
                    + "'name':'"
                    + ObjectType.DATA_ELEMENT
                    + "', "
                    + "'valueType':'INTEGER', "
                    + "'"
                    + ObjectType.DATA_ELEMENT.getPropertyName()
                    + "':true}"));
    JsonObject attr = GET("/attributes/{id}/gist", uid).content();
    assertTrue(attr.getBoolean("dataElementAttribute").booleanValue());
  }

  /** Tests that each type only sets the property the type relates to */
  @Test
  void testObjectTypes() {
    for (ObjectType testedType : ObjectType.values()) {
      String propertyName = testedType.getPropertyName();
      String uid =
          assertStatus(
              HttpStatus.CREATED,
              POST(
                  "/attributes",
                  "{"
                      + "'name':'"
                      + testedType
                      + "', "
                      + "'valueType':'INTEGER', "
                      + "'"
                      + propertyName
                      + "':true}"));
      JsonObject attr = GET("/attributes/{uid}", uid).content();
      for (ObjectType otherType : ObjectType.values()) {
        assertEquals(
            testedType == otherType, attr.getBoolean(otherType.getPropertyName()).booleanValue());
      }
    }
  }
}
