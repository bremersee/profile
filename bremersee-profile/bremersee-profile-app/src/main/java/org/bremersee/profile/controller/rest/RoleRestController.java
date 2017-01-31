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
import org.bremersee.common.business.RoleNameService;
import org.bremersee.common.model.BooleanDto;
import org.bremersee.common.model.StringListDto;
import org.bremersee.pagebuilder.PageBuilderUtils;
import org.bremersee.pagebuilder.model.Page;
import org.bremersee.pagebuilder.model.PageDto;
import org.bremersee.profile.SwaggerConfig;
import org.bremersee.profile.business.RoleService;
import org.bremersee.profile.model.RoleDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Set;

/**
 * @author Christian Bremer
 */
//@Api(authorizations = {
//        @Authorization(
//                value = SwaggerConfig.SECURITY_SCHEMA_OAUTH2,
//                scopes = {
//                        @AuthorizationScope(
//                                scope = SwaggerConfig.OPENID_SCOPE,
//                                description = SwaggerConfig.OPENID_SCOPE_DESCR),
//                        @AuthorizationScope(
//                                scope = SwaggerConfig.PROFILE_SCOPE,
//                                description = SwaggerConfig.PROFILE_SCOPE_DESCR)
//                }
//        )
//})
@RestController
@RequestMapping(path = "/api/role")
public class RoleRestController extends AbstractRestControllerImpl {

    private final RoleService roleService;

    @Autowired
    public RoleRestController(final RoleService roleService) {
        this.roleService = roleService;
    }

    @Override
    protected void doInit() {
        // nothing to init
    }

    @PreAuthorize(HAS_OAUTH2_SCOPE_PROFILE)
    @ApiOperation(value = "Find all roles.")
    @CrossOrigin
    @RequestMapping(
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public PageDto findAll(
            @RequestParam(name = "p", required = false) @ApiParam("The page number.") Integer pageNumber,
            @RequestParam(name = "s", required = false) @ApiParam("The page size.") Integer pageSize,
            @RequestParam(name = "c", required = false) @ApiParam("The comparator item or chain.") String comparatorItem,
            @RequestParam(name = "q", required = false) @ApiParam("A query value.") String query) {

        Page<? extends RoleDto> page = roleService
                .findAll(createPageRequest(pageNumber, pageSize, comparatorItem, query));
        return PageBuilderUtils.createPageDto(page, null);
    }

    @ApiOperation(value = "Create a new role.")
    @CrossOrigin
    @RequestMapping(
            method = RequestMethod.POST,
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<RoleDto> create(
            @RequestBody @ApiParam(value = "The role.") RoleDto role) {

        RoleDto dto = roleService.create(role);
        final URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{roleName}")
                .buildAndExpand(dto.getName()).toUri();
        return ResponseEntity.created(location).body(dto);
    }

    @ApiOperation(value = "Update a role.")
    @CrossOrigin
    @RequestMapping(
            path = "/{roleName}",
            method = RequestMethod.PATCH,
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> update(
            @PathVariable("roleName") @ApiParam(value = "The role name", required = true) String roleName,
            @RequestBody @ApiParam(value = "The role.") RoleDto role) {

        roleService.update(roleName, role);
        return ResponseEntity.noContent().build();
    }

    @ApiOperation(value = "Find a role by it's name.")
    @CrossOrigin
    @RequestMapping(
            path = "/{roleName}",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public RoleDto findByName(
            @PathVariable("roleName") @ApiParam(value = "The role name", required = true) String roleName) {

        return roleService.findByName(roleName);
    }

    @ApiOperation(value = "Test whether a role exists or not.")
    @CrossOrigin
    @RequestMapping(
            path = "/{roleName}/exists",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public BooleanDto existsByName(
            @PathVariable("roleName") @ApiParam(value = "The role name", required = true) String roleName) {

        return new BooleanDto(roleService.existsByName(roleName));
    }

    @ApiOperation(value = "Delete a role.")
    @CrossOrigin
    @RequestMapping(
            path = "/{roleName}",
            method = RequestMethod.DELETE)
    public ResponseEntity<Void> deleteByName(
            @PathVariable("roleName") @ApiParam(value = "The role name", required = true) String roleName) {

        roleService.deleteByName(roleName);
        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "Find roles of an user.")
    @CrossOrigin
    @RequestMapping(
            path = "/f/role-names-by-member",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public StringListDto findRoleNamesByMember(
            @RequestParam(name = "member", required = false)
            @ApiParam(value = "The name of the role member (default is the current user name)") String member,
            @RequestParam(name = "role-name-prefix", required = false)
            @ApiParam(value = "An optional role name prefix", example = RoleNameService.CUSTOM_ROLE_PREFIX)
                    String roleNamePrefix) {

        final String memberName = StringUtils.isBlank(member) ? getCurrentUserName() : member;
        Set<String> roleNames;
        if (StringUtils.isBlank(roleNamePrefix)) {
            roleNames = roleService.findRoleNamesByMember(memberName);
        } else {
            roleNames = roleService.findRoleNamesByMemberAndRoleNamePrefix(memberName, roleNamePrefix);
        }
        return new StringListDto(roleNames);
    }

    @ApiOperation(value = "Test whether the user has the role or not.")
    @CrossOrigin
    @RequestMapping(
            path = "/f/has-role",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public BooleanDto hasRole(
            @RequestParam(name = "user", required = false)
            @ApiParam(value = "The name of the user (default is the current user name)") String user,
            @RequestParam("role-name") @ApiParam(value = "The role name", required = true) String roleName) {

        final String userName = StringUtils.isBlank(user) ? getCurrentUserName() : user;
        return new BooleanDto(roleService.hasRole(userName, roleName));
    }

    @ApiOperation(value = "Get the members of the role.")
    @CrossOrigin
    @RequestMapping(
            path = "/{roleName}/members",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public StringListDto getMembers(
            @PathVariable("roleName") @ApiParam(value = "The role name", required = true) String roleName) {

        return new StringListDto(roleService.getMembers(roleName));
    }

    @ApiOperation(value = "Add members to the role.")
    @CrossOrigin
    @RequestMapping(
            path = "/{roleName}/members",
            method = RequestMethod.PATCH,
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> addMembers(
            @PathVariable("roleName") @ApiParam(value = "The role name", required = true) String roleName,
            @RequestBody @ApiParam(value = "The member(s)", required = true) StringListDto members) {

        roleService.addMembers(roleName, members.getEntries());
        return ResponseEntity.noContent().build();
    }

    @ApiOperation(value = "Removes members from the role.")
    @CrossOrigin
    @RequestMapping(
            path = "/{roleName}/members",
            method = RequestMethod.DELETE)
    public ResponseEntity<Void> removeMembers(
            @PathVariable("roleName") @ApiParam(value = "The role name", required = true) String roleName,
            @RequestParam(name = "member", required = false) @ApiParam("The member(s)") List<String> members) {

        if (members != null && !members.isEmpty()) {
            roleService.removeMembers(roleName, members);
        }
        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "Replace the members of the role.")
    @CrossOrigin
    @RequestMapping(
            path = "/{roleName}/members",
            method = RequestMethod.POST,
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Void> updateMembers(
            @PathVariable("roleName") @ApiParam(value = "The role name", required = true) String roleName,
            @RequestBody @ApiParam(value = "The member(s)", required = true) StringListDto members) {

        roleService.updateMembers(roleName, members.getEntries());
        final URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().build().toUri();
        return ResponseEntity.created(location).build();
    }

}
