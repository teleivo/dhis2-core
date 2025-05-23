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
package org.hisp.dhis.metadata.users;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

import com.google.gson.JsonArray;
import java.util.List;
import org.hisp.dhis.ApiTest;
import org.hisp.dhis.test.e2e.actions.LoginActions;
import org.hisp.dhis.test.e2e.actions.RestApiActions;
import org.hisp.dhis.test.e2e.actions.UserActions;
import org.hisp.dhis.test.e2e.dto.ApiResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class UserLookupTests extends ApiTest {
  private RestApiActions lookupActions;

  private UserActions userActions;

  @BeforeAll
  public void beforeAll() {
    lookupActions = new RestApiActions("/userLookup");
    userActions = new UserActions();

    new LoginActions().loginAsSuperUser();
  }

  @ParameterizedTest
  @CsvSource({"PQD6wXJ2r5j,id", "taadmin,username"})
  public void shouldLookupSpecificUser(String resource, String propertyToValidate) {
    ApiResponse response = lookupActions.get(resource);

    response
        .validate()
        .statusCode(200)
        .body(propertyToValidate, equalTo(resource))
        .body("id", notNullValue())
        .body("username", notNullValue())
        .body("firstName", notNullValue())
        .body("surname", notNullValue())
        .body("displayName", notNullValue());
  }

  @ParameterizedTest
  @ValueSource(strings = {"tasuper", "tasuperadmin", "TA", "TA", "Admin", "Superuser"})
  public void shouldLookupUserWithQuery(String query) {
    ApiResponse response = lookupActions.get("?query=" + query);

    response.validate().statusCode(200).body("users", hasSize(greaterThan(0)));

    JsonArray users = response.extractJsonObject("").getAsJsonArray("users");

    users.forEach(
        user -> {
          String str = user.getAsJsonObject().toString();
          assertThat(str, containsStringIgnoringCase(query));
        });
  }

  @ParameterizedTest
  @ValueSource(strings = {"taadmin@", "@dhis2.org", "tasuperuser@dhis2.org"})
  public void shouldLookupUserByEmail(String query) {
    ApiResponse response = lookupActions.get("?query=" + query);

    response.validate().statusCode(200).body("users", hasSize(greaterThan(0)));

    List<String> users = response.extractList("users.id");

    users.forEach(
        user -> {
          userActions
              .get(user)
              .validate()
              .statusCode(200)
              .body("email", containsStringIgnoringCase(query));
        });
  }
}
