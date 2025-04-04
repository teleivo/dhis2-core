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
package org.hisp.dhis.security.ldap.authentication;

import static java.lang.String.format;

import lombok.extern.slf4j.Slf4j;
import org.hisp.dhis.user.User;
import org.hisp.dhis.user.UserStore;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.support.BaseLdapPathContextSource;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.ldap.authentication.BindAuthenticator;

/**
 * Authenticator which checks whether LDAP authentication is configured. If not, the authentication
 * will be aborted, otherwise authentication is delegated to Spring BindAuthenticator.
 *
 * @author Lars Helge Overland
 */
@Slf4j
public class DhisBindAuthenticator extends BindAuthenticator {

  private UserStore userStore;

  public DhisBindAuthenticator(BaseLdapPathContextSource contextSource, UserStore userStore) {
    super(contextSource);
    this.userStore = userStore;
  }

  @Override
  public DirContextOperations authenticate(Authentication authentication) {
    User user = userStore.getUserByUsername(authentication.getName());
    if (user == null) {
      throw new BadCredentialsException("Incorrect user credentials");
    }

    if (user.hasLdapId() && user.isExternalAuth()) {
      log.debug(
          "LDAP authentication attempt with username: '{}' and LDAP ID: '{}'",
          user.getUsername(),
          user.getLdapId());
      authentication =
          new UsernamePasswordAuthenticationToken(
              user.getLdapId(), authentication.getCredentials());
    } else {
      String msg =
          format(
              "Could not bind user to LDAP host due to missing LDAP ID or external auth: '%s'",
              user.getUsername());
      throw new BadCredentialsException(msg);
    }

    return super.authenticate(authentication);
  }

  @Override
  public void handleBindException(String userDn, String username, Throwable cause) {
    String msg =
        format("Failed to bind to LDAP host with DN: '%s' and username: '%s'", userDn, username);
    log.warn(msg, cause);
    log.debug("LDAP user bind failed", cause);
  }
}
