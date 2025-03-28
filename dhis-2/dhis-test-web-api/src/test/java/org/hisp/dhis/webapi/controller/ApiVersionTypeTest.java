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

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hisp.dhis.test.webapi.WebSpringTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * @author Morten Olav Hansen <mortenoh@gmail.com>
 */
class ApiVersionTypeTest extends WebSpringTestBase {

  private MockHttpServletRequestBuilder get(String urlTemplate, Object... uriVariables) {
    return org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(
        "/api/" + urlTemplate, uriVariables);
  }

  @Test
  void testTypeAnnotationDefault() throws Exception {
    MockHttpSession session = getMockHttpSession();
    String endpoint = "/type/testDefault";
    mvc.perform(get(endpoint).session(session)).andExpect(status().isOk());
    mvc.perform(get("/31" + endpoint).session(session)).andExpect(status().isNotFound());
    mvc.perform(get("/32" + endpoint).session(session)).andExpect(status().isNotFound());
  }

  @Test
  void testTypeAnnotationDefaultV31() throws Exception {
    MockHttpSession session = getMockHttpSession();
    String endpoint = "/type/testDefaultV31";
    mvc.perform(get(endpoint).session(session)).andExpect(status().isOk());
    mvc.perform(get("/31" + endpoint).session(session)).andExpect(status().isOk());
    mvc.perform(get("/32" + endpoint).session(session)).andExpect(status().isNotFound());
  }

  @Test
  void testTypeAnnotationV31V32() throws Exception {
    MockHttpSession session = getMockHttpSession();
    String endpoint = "/type/testV31V32";
    mvc.perform(get(endpoint).session(session)).andExpect(status().isNotFound());
    mvc.perform(get("/31" + endpoint).session(session)).andExpect(status().isOk());
    mvc.perform(get("/32" + endpoint).session(session)).andExpect(status().isOk());
  }

  @Test
  void testTypeAnnotationAll() throws Exception {
    MockHttpSession session = getMockHttpSession();
    String endpoint = "/type/testAll";
    mvc.perform(get(endpoint).session(session)).andExpect(status().isNotFound());
    mvc.perform(get("/31" + endpoint).session(session)).andExpect(status().isOk());
    mvc.perform(get("/32" + endpoint).session(session)).andExpect(status().isOk());
  }

  @Test
  void testTypeAnnotationAllExcludeV32() throws Exception {
    MockHttpSession session = getMockHttpSession();
    String endpoint = "/type/testAllExcludeV32";
    mvc.perform(get(endpoint).session(session)).andExpect(status().isNotFound());
    mvc.perform(get("/31" + endpoint).session(session)).andExpect(status().isOk());
    mvc.perform(get("/32" + endpoint).session(session)).andExpect(status().isNotFound());
  }

  @Test
  void testTypeAnnotationDefaultAll() throws Exception {
    MockHttpSession session = getMockHttpSession();
    String endpoint = "/type/testDefaultAll";
    mvc.perform(get(endpoint).session(session)).andExpect(status().isOk());
    mvc.perform(get("/31" + endpoint).session(session)).andExpect(status().isOk());
    mvc.perform(get("/32" + endpoint).session(session)).andExpect(status().isOk());
  }
}
