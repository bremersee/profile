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
import org.bremersee.profile.business.PosixSettingsService;
import org.bremersee.profile.model.PosixSettingsDto;
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
@RequestMapping(path = "/api/user-profile/{userName}/posix-settings")
public class PosixSettingsRestController extends AbstractRestControllerImpl {

    private final PosixSettingsService posixSettingsService;

    @Autowired
    public PosixSettingsRestController(PosixSettingsService posixSettingsService) {
        this.posixSettingsService = posixSettingsService;
    }

    @Override
    protected void doInit() {
        // nothing to init
    }

    @ApiOperation(value = "Apply posix settings to the user profile.")
    @CrossOrigin
    @RequestMapping(
            method = RequestMethod.POST,
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Void> applyPosixSettingsToProfile(
            @PathVariable("userName") @ApiParam(value = "The user name", required = true) String userName,
            @RequestBody @ApiParam(value = "The settings", required = true) PosixSettingsDto posixSettings) {

        posixSettingsService.applyPosixSettingsToProfile(userName, posixSettings);
        final URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().build().toUri();
        return ResponseEntity.created(location).build();
    }

    @ApiOperation(value = "Update posix settings of the user profile.")
    @CrossOrigin
    @RequestMapping(
            method = RequestMethod.PATCH,
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> updatePosixSettingsToProfile(
            @PathVariable("userName") @ApiParam(value = "The user name", required = true) String userName,
            @RequestBody @ApiParam(value = "The settings", required = true) PosixSettingsDto posixSettings) {

        posixSettingsService.updatePosixSettingsToProfile(userName, posixSettings);
        return ResponseEntity.noContent().build();
    }

    @ApiOperation(value = "Get posix settings of the user profile.")
    @CrossOrigin
    @RequestMapping(
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public PosixSettingsDto findPosixSettingsByUid(
            @PathVariable("userName") @ApiParam(value = "The user name", required = true) String userName) {

        return posixSettingsService.findPosixSettingsByUid(userName);
    }

    @ApiOperation(value = "Get posix settings of the user profile.")
    @CrossOrigin
    @RequestMapping(
            method = RequestMethod.DELETE)
    public ResponseEntity<Void> removePosixSettingsFromProfile(
            @PathVariable("userName") @ApiParam(value = "The user name", required = true) String userName) {
        
        posixSettingsService.removePosixSettingsFromProfile(userName);
        return ResponseEntity.ok().build();
    }

}
