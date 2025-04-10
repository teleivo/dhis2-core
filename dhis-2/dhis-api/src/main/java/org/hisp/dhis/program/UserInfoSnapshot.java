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
package org.hisp.dhis.program;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.hisp.dhis.common.DxfNamespaces;
import org.hisp.dhis.program.notification.template.snapshot.IdentifiableObjectSnapshot;
import org.hisp.dhis.user.User;
import org.hisp.dhis.user.UserDetails;

/**
 * Data structure to hold user information during save/update of events, enrollments and comments
 *
 * @author Giuseppe Nespolino
 */
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoSnapshot extends IdentifiableObjectSnapshot {
  private String username;

  private String firstName;

  private String surname;

  @JsonProperty
  @JacksonXmlProperty(namespace = DxfNamespaces.DXF_2_0)
  @Override
  public String getUid() {
    return super.getUid();
  }

  @JsonProperty
  @JacksonXmlProperty(namespace = DxfNamespaces.DXF_2_0)
  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  @JsonProperty
  @JacksonXmlProperty(namespace = DxfNamespaces.DXF_2_0)
  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  @JsonProperty
  @JacksonXmlProperty(namespace = DxfNamespaces.DXF_2_0)
  public String getSurname() {
    return surname;
  }

  public void setSurname(String surname) {
    this.surname = surname;
  }

  public static UserInfoSnapshot from(User user) {
    UserDetails currentUserDetails = UserDetails.fromUser(user);
    return Optional.ofNullable(currentUserDetails)
        .map(UserInfoSnapshot::toUserInfoSnapshot)
        .orElse(null);
  }

  public static UserInfoSnapshot from(UserDetails user) {
    return Optional.ofNullable(user).map(UserInfoSnapshot::toUserInfoSnapshot).orElse(null);
  }

  private static UserInfoSnapshot toUserInfoSnapshot(UserDetails user) {
    UserInfoSnapshot eventUserInfo =
        new UserInfoSnapshot(user.getUsername(), user.getFirstName(), user.getSurname());
    eventUserInfo.setId(user.getId());
    eventUserInfo.setCode(user.getCode());
    eventUserInfo.setUid(user.getUid());
    return eventUserInfo;
  }

  public static UserInfoSnapshot of(
      long id, String code, String uid, String username, String firstName, String surname) {
    UserInfoSnapshot eventUserInfo = new UserInfoSnapshot(username, firstName, surname);
    eventUserInfo.setId(id);
    eventUserInfo.setCode(code);
    eventUserInfo.setUid(uid);
    return eventUserInfo;
  }
}
