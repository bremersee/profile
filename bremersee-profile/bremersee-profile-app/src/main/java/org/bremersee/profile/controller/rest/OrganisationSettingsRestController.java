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
import org.bremersee.profile.SwaggerConfig;
import org.bremersee.profile.business.OrganisationSettingsService;
import org.bremersee.profile.model.OrganisationSettingsDto;
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
@RequestMapping(path = "/api/user-profile/{userName}/organisation-settings")
public class OrganisationSettingsRestController extends AbstractRestControllerImpl {

    private final OrganisationSettingsService organisationSettingsService;

    @Autowired
    public OrganisationSettingsRestController(OrganisationSettingsService organisationSettingsService) {
        this.organisationSettingsService = organisationSettingsService;
    }

    @Override
    protected void doInit() {
        // nothing to init
    }

    @ApiOperation(value = "Apply organisation settings to the user profile.")
    @CrossOrigin
    @RequestMapping(
            method = RequestMethod.POST,
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Void> applyOrganisationSettingsToProfile(
            @PathVariable("userName") @ApiParam(value = "The user name", required = true) String userName,
            @RequestBody @ApiParam(value = "The settings", required = true)
                    OrganisationSettingsDto organisationSettings) {

        organisationSettingsService.applyOrganisationSettingsToProfile(userName, organisationSettings);
        final URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().build().toUri();
        return ResponseEntity.created(location).build();
    }

    @ApiOperation(value = "Update organisation settings of the user profile.")
    @CrossOrigin
    @RequestMapping(
            method = RequestMethod.PATCH,
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> updateOrganisationSettingsToProfile(
            @PathVariable("userName") @ApiParam(value = "The user name", required = true) String userName,
            @RequestBody @ApiParam(value = "The settings", required = true)
                    OrganisationSettingsDto organisationSettings) {

        organisationSettingsService.updateOrganisationSettingsToProfile(userName, organisationSettings);
        return ResponseEntity.noContent().build();
    }

    @ApiOperation(value = "Get organisation settings of the user profile.")
    @CrossOrigin
    @RequestMapping(
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public OrganisationSettingsDto findOrganisationSettingsByUid(
            @PathVariable("userName") @ApiParam(value = "The user name", required = true) String userName) {

        return organisationSettingsService.findOrganisationSettingsByUid(userName);
    }

    @ApiOperation(value = "Get organisation settings of the user profile.")
    @CrossOrigin
    @RequestMapping(
            method = RequestMethod.DELETE)
    public ResponseEntity<Void> removeOrganisationSettingsFromProfile(
            @PathVariable("userName") @ApiParam(value = "The user name", required = true) String userName) {
        
        organisationSettingsService.removeOrganisationSettingsFromProfile(userName);
        return ResponseEntity.ok().build();
    }

}
