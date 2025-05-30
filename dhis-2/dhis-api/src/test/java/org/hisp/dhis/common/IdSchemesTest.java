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
package org.hisp.dhis.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class IdSchemesTest {
  @Test
  void testGetIdScheme() {
    IdSchemes schemes = new IdSchemes();
    schemes.setDataElementIdScheme(IdScheme.UID.name());
    schemes.setIdScheme(IdScheme.CODE.name());
    assertEquals(IdScheme.UID, schemes.getDataElementIdScheme());
    assertEquals(IdScheme.CODE, schemes.getOrgUnitIdScheme());
    assertEquals(IdScheme.CODE, schemes.getIdScheme());
  }

  @Test
  void testFrom() {
    IdScheme schemeA = IdScheme.from(IdScheme.ATTR_ID_SCHEME_PREFIX + "abcdefghijA");
    IdScheme schemeB = IdScheme.from("CODE");
    assertEquals(IdentifiableProperty.ATTRIBUTE, schemeA.getIdentifiableProperty());
    assertEquals("abcdefghijA", schemeA.getAttribute());
    assertEquals(IdentifiableProperty.CODE, schemeB.getIdentifiableProperty());
  }

  @Test
  void testSerializeIdSchemes() throws JsonProcessingException {
    IdSchemes original = new IdSchemes();
    original.setProgramIdScheme("CODE");
    // language=JSON
    String expected =
        """
        {"programIdScheme":{"type":"CODE"}}""";
    assertEquals(expected, new ObjectMapper().writeValueAsString(original));
  }

  @Test
  void testDeserializeIdSchemes() throws JsonProcessingException {
    IdSchemes expected = new IdSchemes();
    expected.setProgramIdScheme("CODE");

    ObjectMapper mapper = new ObjectMapper();
    IdSchemes actual = mapper.readValue(mapper.writeValueAsString(expected), IdSchemes.class);
    assertEquals(expected, actual);
    assertEquals(IdentifiableProperty.CODE, actual.getProgramIdScheme().getIdentifiableProperty());
  }
}
