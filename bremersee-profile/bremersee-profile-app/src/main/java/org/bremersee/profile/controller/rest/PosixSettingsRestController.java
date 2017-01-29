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
import org.bremersee.common.spring.autoconfigure.RestConstants;
import org.bremersee.profile.business.PosixSettingsService;
import org.bremersee.profile.model.PosixSettingsDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
@RequestMapping(path = RestConstants.REST_CONTEXT_PATH + "/user-profile")
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
            params = "/{userName}/posix-settings",
            method = RequestMethod.PUT,
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<Void> applyPosixSettingsToProfile(
            @PathVariable("userName") @ApiParam(value = "The user name", required = true) String userName,
            @RequestBody @ApiParam(value = "The settings", required = true) PosixSettingsDto posixSettings) {
        posixSettingsService.applyPosixSettingsToProfile(userName, posixSettings);
        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "Update posix settings of the user profile.")
    @CrossOrigin
    @RequestMapping(
            params = "/{userName}/posix-settings",
            method = RequestMethod.POST,
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<Void> updatePosixSettingsToProfile(
            @PathVariable("userName") @ApiParam(value = "The user name", required = true) String userName,
            @RequestBody @ApiParam(value = "The settings", required = true) PosixSettingsDto posixSettings) {
        posixSettingsService.updatePosixSettingsToProfile(userName, posixSettings);
        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "Get posix settings of the user profile.")
    @CrossOrigin
    @RequestMapping(
            params = "/{userName}/posix-settings",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public PosixSettingsDto findPosixSettingsByUid(
            @PathVariable("userName") @ApiParam(value = "The user name", required = true) String userName) {
        return posixSettingsService.findPosixSettingsByUid(userName);
    }

    @ApiOperation(value = "Get posix settings of the user profile.")
    @CrossOrigin
    @RequestMapping(
            params = "/{userName}/posix-settings",
            method = RequestMethod.DELETE)
    public ResponseEntity<Void> removePosixSettingsFromProfile(
            @PathVariable("userName") @ApiParam(value = "The user name", required = true) String userName) {
        posixSettingsService.removePosixSettingsFromProfile(userName);
        return ResponseEntity.ok().build();
    }

}
