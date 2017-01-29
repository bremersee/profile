/*
 * Copyright 2015 the original author or authors.
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
import org.apache.commons.lang3.StringUtils;
import org.bremersee.common.model.BooleanDto;
import org.bremersee.common.model.StringListDto;
import org.bremersee.common.spring.autoconfigure.RestConstants;
import org.bremersee.pagebuilder.PageBuilderUtils;
import org.bremersee.pagebuilder.model.Page;
import org.bremersee.pagebuilder.model.PageDto;
import org.bremersee.profile.business.UserGroupService;
import org.bremersee.profile.model.UserGroupDto;
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
@RequestMapping(path = RestConstants.REST_CONTEXT_PATH + "/user-group")
public class UserGroupRestController extends AbstractRestControllerImpl {

    private UserGroupService userGroupService;

    @Autowired
    public UserGroupRestController(final UserGroupService userGroupService) {
        this.userGroupService = userGroupService;
    }

    @Override
    protected void doInit() {
        // nothing to init
    }

    @ApiOperation(value = "Finds all user groups.")
    @CrossOrigin
    @RequestMapping(
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public PageDto findAll(
            @RequestParam(name = "p", required = false) @ApiParam("The page number.") Integer pageNumber,
            @RequestParam(name = "s", required = false) @ApiParam("The page size.") Integer pageSize,
            @RequestParam(name = "c", required = false) @ApiParam("The comparator item or chain.") String comparatorItem,
            @RequestParam(name = "q", required = false) @ApiParam("A query value.") String query) {

        Page<UserGroupDto> page = userGroupService.findAll(
                createPageRequest(pageNumber, pageSize, comparatorItem, query));
        return PageBuilderUtils.createPageDto(page, null);
    }

    @ApiOperation(value = "Creates a new user group.")
    @CrossOrigin
    @RequestMapping(
            method = RequestMethod.PUT,
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public UserGroupDto create(
            @RequestBody @ApiParam(value = "The user group.") UserGroupDto userGroup) {

        return userGroupService.create(userGroup);
    }

    @ApiOperation(value = "Updates a user group.")
    @CrossOrigin
    @RequestMapping(
            path = "/{userGroupName}",
            method = RequestMethod.PUT,
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public UserGroupDto update(
            @PathVariable("userGroupName") @ApiParam(value = "The group name", required = true) String userGroupName,
            @RequestBody @ApiParam(value = "The user group.") UserGroupDto userGroup) {

        return userGroupService.update(userGroupName, userGroup);
    }

    @ApiOperation(value = "Finds a user group by it's identifier (name, GID or Samba SID).")
    @CrossOrigin
    @RequestMapping(
            path = "/{identifier}",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public UserGroupDto findByIdentifier(
            @PathVariable("identifier") @ApiParam(value = "The group identifier", required = true) String identifier) {

        return userGroupService.findByIdentifier(identifier);
    }

    @ApiOperation(value = "Tests whether a user group exists or not.")
    @CrossOrigin
    @RequestMapping(
            path = "/{identifier}/exists",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public BooleanDto existsByIdentifier(
            @PathVariable("identifier") @ApiParam(value = "The group identifier", required = true) String identifier) {

        return new BooleanDto(userGroupService.existsByIdentifier(identifier));
    }

    @ApiOperation(value = "Deletes a user group.")
    @CrossOrigin
    @RequestMapping(
            path = "/{userGroupName}",
            method = RequestMethod.DELETE)
    public ResponseEntity<Void> deleteByName(
            @PathVariable("userGroupName") @ApiParam(value = "The group name", required = true) String userGroupName) {

        userGroupService.deleteByName(userGroupName);
        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "Finds all groups which contain the specified user as member.")
    @CrossOrigin
    @RequestMapping(
            path = "/f/group-names-by-member",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public StringListDto findUserGroupNamesByMember(
            @RequestParam(name = "member", required = false)
            @ApiParam("The group member (default is the current user)") String member) {

        final String memberName = StringUtils.isBlank(member) ? getCurrentUserName() : member;
        return new StringListDto(userGroupService.findUserGroupNamesByMember(memberName));
    }

    @ApiOperation(value = "Tests whether the specified user is a member of the specified role.")
    @CrossOrigin
    @RequestMapping(
            path = "/f/is-group-member",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public BooleanDto isGroupMember(
            @RequestParam(name = "member") @ApiParam("The group member (default is the current user)") String member,
            @RequestParam(name = "group-name")
            @ApiParam(value = "The group name", required = true) String userGroupName) {

        final String memberName = StringUtils.isBlank(member) ? getCurrentUserName() : member;
        return new BooleanDto(userGroupService.isGroupMember(memberName, userGroupName));
    }

    @ApiOperation(value = "Returns the members of the specified user group.")
    @CrossOrigin
    @RequestMapping(
            path = "/{userGroupName}/members",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public StringListDto getMembers(
            @PathVariable("userGroupName") @ApiParam(value = "The group name", required = true) String userGroupName) {

        return new StringListDto(userGroupService.getMembers(userGroupName));
    }

    @ApiOperation(value = "Adds members to the specified user group.")
    @CrossOrigin
    @RequestMapping(
            path = "/{userGroupName}/members",
            method = RequestMethod.PUT,
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<Void> addMembers(
            @PathVariable("userGroupName") @ApiParam(value = "The group name", required = true) String userGroupName,
            @RequestBody @ApiParam(value = "The member(s) to add", required = true) StringListDto members) {

        userGroupService.addMembers(userGroupName, members.getEntries());
        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "Removes members from the specified user group.")
    @CrossOrigin
    @RequestMapping(
            path = "/{userGroupName}/members",
            method = RequestMethod.DELETE)
    public ResponseEntity<Void> removeMembers(
            @PathVariable("userGroupName") @ApiParam(value = "The group name", required = true) String userGroupName,
            @RequestParam(name = "member", required = false)
            @ApiParam("The member(s) to remove") List<String> members) {

        if (members != null && !members.isEmpty()) {
            userGroupService.removeMembers(userGroupName, members);
        }
        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "Replaces the members of the specified user group.")
    @CrossOrigin
    @RequestMapping(
            path = "/{userGroupName}/members",
            method = RequestMethod.POST,
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<Void> updateMembers(
            @PathVariable("userGroupName") @ApiParam(value = "The group name", required = true) String userGroupName,
            @RequestBody @ApiParam(value = "The new member(s)", required = true) StringListDto members) {

        userGroupService.addMembers(userGroupName, members.getEntries());
        return ResponseEntity.ok().build();
    }

}
