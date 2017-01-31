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
import org.bremersee.pagebuilder.PageBuilderUtils;
import org.bremersee.pagebuilder.model.Page;
import org.bremersee.pagebuilder.model.PageDto;
import org.bremersee.profile.SwaggerConfig;
import org.bremersee.profile.business.UserGroupService;
import org.bremersee.profile.model.UserGroupDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * @author Christian Bremer
 */
@RestController
@RequestMapping(path = "/api/user-group")
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

    @ApiOperation(value = "Find all user groups.")
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

    @ApiOperation(value = "Create a new user group.")
    @CrossOrigin
    @RequestMapping(
            method = RequestMethod.POST,
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<UserGroupDto> create(
            @RequestBody @ApiParam(value = "The user group.") UserGroupDto userGroup) {

        UserGroupDto dto = userGroupService.create(userGroup);
        final URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{userGroupName}")
                .buildAndExpand(dto.getName()).toUri();
        return ResponseEntity.created(location).body(dto);
    }

    @ApiOperation(value = "Update a user group.")
    @CrossOrigin
    @RequestMapping(
            path = "/{userGroupName}",
            method = RequestMethod.PATCH,
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> update(
            @PathVariable("userGroupName") @ApiParam(value = "The group name", required = true) String userGroupName,
            @RequestBody @ApiParam(value = "The user group.") UserGroupDto userGroup) {

        userGroupService.update(userGroupName, userGroup);
        return ResponseEntity.noContent().build();
    }

    @ApiOperation(value = "Find a user group by it's identifier (name, GID or Samba SID).")
    @CrossOrigin
    @RequestMapping(
            path = "/{identifier}",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public UserGroupDto findByIdentifier(
            @PathVariable("identifier") @ApiParam(value = "The group identifier", required = true) String identifier) {

        return userGroupService.findByIdentifier(identifier);
    }

    @ApiOperation(value = "Test whether a user group exists or not.")
    @CrossOrigin
    @RequestMapping(
            path = "/{identifier}/exists",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public BooleanDto existsByIdentifier(
            @PathVariable("identifier") @ApiParam(value = "The group identifier", required = true) String identifier) {

        return new BooleanDto(userGroupService.existsByIdentifier(identifier));
    }

    @ApiOperation(value = "Delete a user group.")
    @CrossOrigin
    @RequestMapping(
            path = "/{userGroupName}",
            method = RequestMethod.DELETE)
    public ResponseEntity<Void> deleteByName(
            @PathVariable("userGroupName") @ApiParam(value = "The group name", required = true) String userGroupName) {

        userGroupService.deleteByName(userGroupName);
        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "Find all groups which contain the specified user as member.")
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

    @ApiOperation(value = "Test whether the specified user is a member of the specified role.")
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

    @ApiOperation(value = "Get the members of the specified user group.")
    @CrossOrigin
    @RequestMapping(
            path = "/{userGroupName}/members",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public StringListDto getMembers(
            @PathVariable("userGroupName") @ApiParam(value = "The group name", required = true) String userGroupName) {

        return new StringListDto(userGroupService.getMembers(userGroupName));
    }

    @ApiOperation(value = "Add members to the specified user group.")
    @CrossOrigin
    @RequestMapping(
            path = "/{userGroupName}/members",
            method = RequestMethod.PATCH,
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> addMembers(
            @PathVariable("userGroupName") @ApiParam(value = "The group name", required = true) String userGroupName,
            @RequestBody @ApiParam(value = "The member(s) to add", required = true) StringListDto members) {

        userGroupService.addMembers(userGroupName, members.getEntries());
        return ResponseEntity.noContent().build();
    }

    @ApiOperation(value = "Remove members from the specified user group.")
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

    @ApiOperation(value = "Replace the members of the specified user group.")
    @CrossOrigin
    @RequestMapping(
            path = "/{userGroupName}/members",
            method = RequestMethod.POST,
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Void> updateMembers(
            @PathVariable("userGroupName") @ApiParam(value = "The group name", required = true) String userGroupName,
            @RequestBody @ApiParam(value = "The new member(s)", required = true) StringListDto members) {

        userGroupService.addMembers(userGroupName, members.getEntries());
        final URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().build().toUri();
        return ResponseEntity.created(location).build();
    }

}
