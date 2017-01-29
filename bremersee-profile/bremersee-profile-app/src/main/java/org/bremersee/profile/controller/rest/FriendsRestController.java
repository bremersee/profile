/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bremersee.profile.controller.rest;

import io.swagger.annotations.*;
import org.bremersee.common.model.StringListDto;
import org.bremersee.common.spring.autoconfigure.RestConstants;
import org.bremersee.profile.business.FriendsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Christian Bremer
 */
@Api(authorizations = {
        @Authorization(
                value = RestConstants.SECURITY_SCHEMA_OAUTH2,
                scopes = {
                        @AuthorizationScope(
                                scope = RestConstants.AUTHORIZATION_SCOPE,
                                description = RestConstants.AUTHORIZATION_SCOPE_DESCR)
                }
        )
})
@RestController
@RequestMapping(path = RestConstants.REST_CONTEXT_PATH + "/friends")
public class FriendsRestController extends AbstractRestControllerImpl {

    private final FriendsService friendsService;

    @Autowired
    public FriendsRestController(FriendsService friendsService) {
        this.friendsService = friendsService;
    }

    @Override
    protected void doInit() {
        // nothing to init
    }

    @ApiOperation(value = "Gets the friends of the user.")
    @CrossOrigin
    @RequestMapping(
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public StringListDto getFriends(
            @RequestParam(name = "user", required = false) @ApiParam("The user name (default is the current user " +
                    "- this parameter can only be set by administrators).") String user) {
        return new StringListDto(friendsService.getFriends(user));
    }

    @ApiOperation(value = "Replaces the friends of the user.")
    @CrossOrigin
    @RequestMapping(
            method = RequestMethod.POST,
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<Void> updateFriends(
            @RequestParam(name = "user", required = false) @ApiParam("The user name (default is the current user " +
                    "- this parameter can only be set by administrators).") String user,
            @RequestBody @ApiParam(value = "The new friends", required = true) StringListDto friends) {

        friendsService.updateFriends(user, friends.getEntries());
        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "Add friends to the user.")
    @CrossOrigin
    @RequestMapping(
            method = RequestMethod.PUT,
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<Void> addFriends(
            @RequestParam(name = "user", required = false) @ApiParam("The user name (default is the current user " +
                    "- this parameter can only be set by administrators).") String user,
            @RequestBody @ApiParam(value = "The friends to add", required = true) StringListDto friends) {

        friendsService.addFriends(user, friends.getEntries());
        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "Remove friends from the user.")
    @CrossOrigin
    @RequestMapping(
            method = RequestMethod.DELETE)
    public ResponseEntity<Void> removeFriends(
            @RequestParam(name = "user", required = false) @ApiParam("The user name (default is the current user " +
                    "- this parameter can only be set by administrators).") String user,
            @RequestParam(name = "friend", required = false) @ApiParam("The friends to remove") List<String> friends) {

        if (friends != null && !friends.isEmpty()) {
            friendsService.removeFriends(user, friends);
        }
        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "Get users who have the specified (current) user as friend.")
    @CrossOrigin
    @RequestMapping(
            params = "/vice-versa",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public StringListDto getUsersWhoHaveMeAsFriend(
            @RequestParam(name = "user", required = false) @ApiParam("The user name (default is the current user " +
                    "- this parameter can only be set by administrators).") String user) {
        return new StringListDto(friendsService.getUsersWhoHaveMeAsFriend(user));
    }

}
