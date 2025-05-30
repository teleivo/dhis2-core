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
package org.hisp.dhis.security;

import static java.lang.System.currentTimeMillis;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import org.hisp.dhis.test.integration.PostgresIntegrationTestBase;
import org.hisp.dhis.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests the effects of {@link User#setDisabled(boolean)} or {@link User#setAccountExpiry(Date)} on
 * the {@link UserDetails} ability to log in.
 *
 * @author Jan Bernitt
 */
@Transactional
class UserDetailsServiceTest extends PostgresIntegrationTestBase {

  @Autowired private UserDetailsService userDetailsService;

  private User user;

  @BeforeEach
  void setUp() {
    user = makeUser("A");
    userService.addUser(user);
  }

  @Test
  void baseline() {
    // a vanilla user should be able to log in
    assertCanLogin(getUserDetails());
  }

  @Test
  void disabledUserCanNotLogIn() {
    user.setDisabled(true);
    userService.updateUser(user);
    assertCanNotLogin(getUserDetails());
  }

  @Test
  void enabledUserCanLogIn() {
    user.setDisabled(true);
    userService.updateUser(user);
    assertCanNotLogin(getUserDetails());

    user.setDisabled(false);
    userService.updateUser(user);
    assertCanLogin(getUserDetails());
  }

  @Test
  void expiredUserAccountCanNotLogIn() {
    // expired 1000s in past
    user.setAccountExpiry(new Date(currentTimeMillis() - 1000));
    userService.updateUser(user);
    assertCanNotLogin(getUserDetails());
  }

  @Test
  void notYetExpiredUserAccountCanStillLogIn() {
    user.setAccountExpiry(new Date(currentTimeMillis() + 10000));
    userService.updateUser(user);
    assertCanLogin(getUserDetails());
  }

  private UserDetails getUserDetails() {
    return userDetailsService.loadUserByUsername(user.getUsername());
  }

  private static void assertCanLogin(UserDetails details) {
    assertTrue(details.isEnabled());
    assertTrue(details.isAccountNonExpired());
    assertTrue(details.isAccountNonLocked());
    assertTrue(details.isCredentialsNonExpired());
  }

  private static void assertCanNotLogin(UserDetails details) {
    assertFalse(
        details.isEnabled()
            && details.isAccountNonExpired()
            && details.isAccountNonLocked()
            && details.isCredentialsNonExpired());
  }
}
