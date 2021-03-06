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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

/**
 * @author Christian Bremer
 */
@RestController
@RequestMapping(path = "/api/custom-role")
public class CustomRoleRestController extends AbstractRestControllerImpl {

    private final RoleService roleService;

    @Autowired
    public CustomRoleRestController(final RoleService roleService) {
        this.roleService = roleService;
    }

    @Override
    protected void doInit() {
        // nothing to init
    }

    @ApiOperation(value = "Create a role of a user which is owned by the user and can be administrated by the user.")
    @CrossOrigin
    @RequestMapping(
            method = RequestMethod.POST,
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<RoleDto> createCustomRole(
            @RequestParam(name = "description", required = false) @ApiParam("The role description.") String description,
            @RequestParam(name = "owner", required = false)
            @ApiParam("The role owner (default is the current user " +
                    "- this parameter can only be set by administrators).") String owner) {

        RoleDto dto = roleService.createCustomRole(description, owner);

        String uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{roleName}")
                .buildAndExpand(dto.getName()).toString();
        uri = uri.replaceFirst("/api/custom-role", "/api/role");
        int index = uri.indexOf('?');
        if (index > 0) {
            uri = uri.substring(0, index);
        }

        return ResponseEntity.created(URI.create(uri)).body(dto);
    }

    @ApiOperation(value = "Find custom roles.")
    @CrossOrigin
    @RequestMapping(
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public PageDto findCustomRoles(
            @RequestParam(name = "p", required = false) @ApiParam("The page number.") Integer pageNumber,
            @RequestParam(name = "s", required = false) @ApiParam("The page size.") Integer pageSize,
            @RequestParam(name = "c", required = false) @ApiParam("The comparator item or chain.") String comparatorItem,
            @RequestParam(name = "q", required = false) @ApiParam("A query value.") String query,
            @RequestParam(name = "owner", required = false) @ApiParam("The owner of the custom roles " +
                    "(default is the current user - this parameter can only be set by administrators).") String owner) {

        Page<? extends RoleDto> page = roleService
                .findCustomRoles(createPageRequest(pageNumber, pageSize, comparatorItem, query), owner);
        return PageBuilderUtils.createPageDto(page, null);
    }

    @ApiOperation(value = "Deletes all custom roles of the owner.")
    @CrossOrigin
    @RequestMapping(
            method = RequestMethod.DELETE)
    public ResponseEntity<Void> deleteCustomRoles(
            @RequestParam(name = "owner", required = false) @ApiParam("The owner of the custom roles " +
                    "(default is the current user - this parameter can only be set by administrators).") String owner) {

        roleService.deleteCustomRoles(owner);
        return ResponseEntity.ok().build();
    }

}
