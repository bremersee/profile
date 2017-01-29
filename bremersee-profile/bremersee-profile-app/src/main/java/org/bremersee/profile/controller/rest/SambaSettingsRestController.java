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
import org.bremersee.profile.business.SambaSettingsService;
import org.bremersee.profile.model.SambaSettingsDto;
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

    @ApiOperation(value = "Apply samba settings to the user profile.")
    @CrossOrigin
    @RequestMapping(
            params = "/{userName}/samba-settings",
            method = RequestMethod.PUT,
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<Void> applySambaSettingsToProfile(
            @PathVariable("userName") @ApiParam(value = "The user name", required = true) String userName,
            @RequestBody @ApiParam(value = "The settings", required = true) SambaSettingsDto sambaSettings) {
        sambaSettingsService.applySambaSettingsToProfile(userName, sambaSettings);
        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "Update samba settings of the user profile.")
    @CrossOrigin
    @RequestMapping(
            params = "/{userName}/samba-settings",
            method = RequestMethod.POST,
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<Void> updateSambaSettingsToProfile(
            @PathVariable("userName") @ApiParam(value = "The user name", required = true) String userName,
            @RequestBody @ApiParam(value = "The settings", required = true) SambaSettingsDto sambaSettings) {
        sambaSettingsService.updateSambaSettingsToProfile(userName, sambaSettings);
        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "Get samba settings of the user profile.")
    @CrossOrigin
    @RequestMapping(
            params = "/{userName}/samba-settings",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public SambaSettingsDto findSambaSettingsByUid(
            @PathVariable("userName") @ApiParam(value = "The user name", required = true) String userName) {
        sambaSettingsService.findSambaSettingsByUid(userName);
        return null;
    }

    @ApiOperation(value = "Get samba settings of the user profile.")
    @CrossOrigin
    @RequestMapping(
            params = "/{userName}/samba-settings",
            method = RequestMethod.DELETE)
    public ResponseEntity<Void> removeSambaSettingsFromProfile(
            @PathVariable("userName") @ApiParam(value = "The user name", required = true) String userName) {
        sambaSettingsService.removeSambaSettingsFromProfile(userName);
        return ResponseEntity.ok().build();
    }

}
