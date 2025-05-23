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
package org.hisp.dhis.webapi.controller.user;

import com.google.common.collect.Sets;
import java.util.List;
import java.util.stream.Collectors;
import org.hisp.dhis.common.IllegalQueryException;
import org.hisp.dhis.common.OpenApi;
import org.hisp.dhis.common.UserOrgUnitType;
import org.hisp.dhis.configuration.ConfigurationService;
import org.hisp.dhis.feedback.ErrorCode;
import org.hisp.dhis.feedback.ErrorMessage;
import org.hisp.dhis.user.User;
import org.hisp.dhis.user.UserGroup;
import org.hisp.dhis.user.UserLookup;
import org.hisp.dhis.user.UserQueryParams;
import org.hisp.dhis.user.UserService;
import org.hisp.dhis.webapi.webdomain.user.UserLookups;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * The user lookup API provides a minimal user information endpoint.
 *
 * @author Lars Helge Overland
 */
@OpenApi.Document(
    entity = User.class,
    group = OpenApi.Document.GROUP_QUERY,
    classifiers = {"team:platform", "purpose:metadata"})
@RestController
@RequestMapping("/api/userLookup")
public class UserLookupController {

  private final UserService userService;

  private final ConfigurationService config;

  public UserLookupController(UserService userService, ConfigurationService config) {
    this.userService = userService;
    this.config = config;
  }

  @GetMapping(value = "/{id}")
  public UserLookup lookUpUser(@PathVariable String id) {
    User user = userService.getUserByIdentifier(id);

    return user != null ? UserLookup.fromUser(user) : null;
  }

  @GetMapping
  public UserLookups lookUpUsers(
      @RequestParam String query,
      @RequestParam(required = false) @OpenApi.Param(UserOrgUnitType.class)
          String orgUnitBoundary) {
    UserQueryParams params =
        new UserQueryParams()
            .setQuery(query)
            .setCanSeeOwnRoles(true)
            .setOrgUnitBoundary(UserOrgUnitType.fromValue(orgUnitBoundary))
            .setMax(25);

    List<UserLookup> users =
        userService.getUsers(params).stream()
            .map(UserLookup::fromUser)
            .collect(Collectors.toList());

    return new UserLookups(users);
  }

  @GetMapping(value = "/feedbackRecipients")
  public UserLookups lookUpFeedbackRecipients(@RequestParam String query) {
    UserGroup feedbackRecipients = config.getConfiguration().getFeedbackRecipients();

    if (feedbackRecipients == null) {
      throw new IllegalQueryException(new ErrorMessage(ErrorCode.E6200));
    }

    UserQueryParams params =
        new UserQueryParams()
            .setQuery(query)
            .setUserGroups(Sets.newHashSet(feedbackRecipients))
            .setCanSeeOwnRoles(true)
            .setMax(25);

    List<UserLookup> users =
        userService.getUsers(params).stream()
            .map(UserLookup::fromUser)
            .collect(Collectors.toList());

    return new UserLookups(users);
  }
}
