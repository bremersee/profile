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
import org.bremersee.profile.business.SambaSettingsService;
import org.bremersee.profile.model.SambaSettingsDto;
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
@RequestMapping(path = "/api/user-profile/{userName}/samba-settings")
public class SambaSettingsRestController extends AbstractRestControllerImpl {

    private final SambaSettingsService sambaSettingsService;

    @Autowired
    public SambaSettingsRestController(SambaSettingsService sambaSettingsService) {
        this.sambaSettingsService = sambaSettingsService;
    }

    @Override
    protected void doInit() {
        // nothing to init
    }

    @ApiOperation(value = "Apply samba settings to the user profile.", httpMethod = "PUT")
    @CrossOrigin
    @RequestMapping(
            method = RequestMethod.POST,
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Void> applySambaSettingsToProfile(
            @PathVariable("userName") @ApiParam(value = "The user name", required = true) String userName,
            @RequestBody @ApiParam(value = "The settings", required = true) SambaSettingsDto sambaSettings) {

        sambaSettingsService.applySambaSettingsToProfile(userName, sambaSettings);
        final URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().build().toUri();
        return ResponseEntity.created(location).build();
    }

    @ApiOperation(value = "Update samba settings of the user profile.", httpMethod = "POST")
    @CrossOrigin
    @RequestMapping(
            method = RequestMethod.PATCH,
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> updateSambaSettingsToProfile(
            @PathVariable("userName") @ApiParam(value = "The user name", required = true) String userName,
            @RequestBody @ApiParam(value = "The settings", required = true) SambaSettingsDto sambaSettings) {

        sambaSettingsService.updateSambaSettingsToProfile(userName, sambaSettings);
        return ResponseEntity.noContent().build();
    }

    @ApiOperation(value = "Get samba settings of the user profile.", httpMethod = "GET")
    @CrossOrigin
    @RequestMapping(
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public SambaSettingsDto findSambaSettingsByUid(
            @PathVariable("userName") @ApiParam(value = "The user name", required = true) String userName) {

        sambaSettingsService.findSambaSettingsByUid(userName);
        return null;
    }

    @ApiOperation(value = "Remove samba settings from the user profile.", httpMethod = "DELETE")
    @CrossOrigin
    @RequestMapping(
            method = RequestMethod.DELETE)
    public ResponseEntity<Void> removeSambaSettingsFromProfile(
            @PathVariable("userName") @ApiParam(value = "The user name", required = true) String userName) {
        
        sambaSettingsService.removeSambaSettingsFromProfile(userName);
        return ResponseEntity.ok().build();
    }

}
