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

import static java.util.Collections.singletonList;
import static org.hisp.dhis.appmanager.AndroidSettingsApp.AUTHORITY;
import static org.hisp.dhis.appmanager.AndroidSettingsApp.NAMESPACE;
import static org.hisp.dhis.http.HttpAssertions.assertStatus;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.hisp.dhis.datastore.DatastoreService;
import org.hisp.dhis.http.HttpStatus;
import org.hisp.dhis.test.webapi.H2ControllerIntegrationTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

/**
 * This tests verifies the protection of the {@link
 * org.hisp.dhis.appmanager.AndroidSettingsApp#NAMESPACE} in the {@link DatastoreService} that is
 * installed on startup.
 *
 * @author Jan Bernitt
 */
@Transactional
class DatastoreControllerAndroidSettingsAppTest extends H2ControllerIntegrationTestBase {

  @BeforeEach
  void setUp() {
    assertStatus(HttpStatus.CREATED, POST("/dataStore/" + NAMESPACE + "/key", "['yes']"));
  }

  /** Everyone can read the keys */
  @Test
  void testGetKeysInNamespace_DefaultPublicAccess() {
    switchToNewUser("not-an-android-manager");
    assertEquals(singletonList("key"), GET("/dataStore/" + NAMESPACE).content().stringValues());
  }

  /** Everyone can read the value */
  @Test
  void testGetKeyJsonValue() {
    switchToNewUser("not-an-android-manager");
    assertEquals(
        singletonList("yes"), GET("/dataStore/" + NAMESPACE + "/key").content().stringValues());
  }

  /** Everyone can read the meta data */
  @Test
  void testGetKeyJsonValueMetaData() {
    switchToNewUser("not-an-android-manager");
    assertStatus(HttpStatus.OK, GET("/dataStore/" + NAMESPACE + "/key/metaData"));
  }

  /**
   * Only user with {@link org.hisp.dhis.appmanager.AndroidSettingsApp#AUTHORITY} can delete the NS.
   */
  @Test
  void testDeleteNamespace() {
    switchToNewUser("not-an-android-manager");
    assertEquals(
        "Namespace 'ANDROID_SETTINGS_APP' is protected, access denied",
        DELETE("/dataStore/" + NAMESPACE).error(HttpStatus.FORBIDDEN).getMessage());
    switchToNewUser("andriod-manager", AUTHORITY);
    assertStatus(HttpStatus.OK, DELETE("/dataStore/" + NAMESPACE));
  }

  /**
   * Only user with {@link org.hisp.dhis.appmanager.AndroidSettingsApp#AUTHORITY} can add to the NS.
   */
  @Test
  void testAddKeyJsonValue() {
    switchToNewUser("not-an-android-manager");
    assertEquals(
        "Namespace 'ANDROID_SETTINGS_APP' is protected, access denied",
        POST("/dataStore/" + NAMESPACE + "/new-key", "[]")
            .error(HttpStatus.FORBIDDEN)
            .getMessage());
    switchToNewUser("andriod-manager", AUTHORITY);
    assertStatus(HttpStatus.CREATED, POST("/dataStore/" + NAMESPACE + "/new-key", "[]"));
  }

  @Test
  void testDeleteKeyJsonValue() {
    switchToNewUser("not-an-android-manager");
    assertEquals(
        "Namespace 'ANDROID_SETTINGS_APP' is protected, access denied",
        DELETE("/dataStore/" + NAMESPACE + "/key").error(HttpStatus.FORBIDDEN).getMessage());
    switchToNewUser("andriod-manager", AUTHORITY);
    assertStatus(HttpStatus.OK, DELETE("/dataStore/" + NAMESPACE + "/key"));
  }
}
