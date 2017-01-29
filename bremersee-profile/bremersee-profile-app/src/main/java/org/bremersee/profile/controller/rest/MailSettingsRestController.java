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
import org.bremersee.profile.business.MailSettingsService;
import org.bremersee.profile.model.MailSettingsDto;
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
public class MailSettingsRestController extends AbstractRestControllerImpl {

    private final MailSettingsService mailSettingsService;

    @Autowired
    public MailSettingsRestController(MailSettingsService mailSettingsService) {
        this.mailSettingsService = mailSettingsService;
    }

    @Override
    protected void doInit() {
        // nothing to init
    }

    @ApiOperation(value = "Apply mail settings to the user profile.")
    @CrossOrigin
    @RequestMapping(
            params = "/{userName}/mail-settings",
            method = RequestMethod.PUT,
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<Void> applyMailSettingsToProfile(
            @PathVariable("userName") @ApiParam(value = "The user name", required = true) String userName,
            @RequestBody @ApiParam(value = "The settings", required = true) MailSettingsDto mailSettings) {
        mailSettingsService.applyMailSettingsToProfile(userName, mailSettings);
        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "Update mail settings of the user profile.")
    @CrossOrigin
    @RequestMapping(
            params = "/{userName}/mail-settings",
            method = RequestMethod.POST,
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<Void> updateMailSettingsToProfile(
            @PathVariable("userName") @ApiParam(value = "The user name", required = true) String userName,
            @RequestBody @ApiParam(value = "The settings", required = true) MailSettingsDto mailSettings) {
        mailSettingsService.updateMailSettingsToProfile(userName, mailSettings);
        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "Get mail settings of the user profile.")
    @CrossOrigin
    @RequestMapping(
            params = "/{userName}/mail-settings",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public MailSettingsDto findMailSettingsByUid(
            @PathVariable("userName") @ApiParam(value = "The user name", required = true) String userName) {
        return mailSettingsService.findMailSettingsByUid(userName);
    }

    @ApiOperation(value = "Get mail settings of the user profile.")
    @CrossOrigin
    @RequestMapping(
            params = "/{userName}/mail-settings",
            method = RequestMethod.DELETE)
    public ResponseEntity<Void> removeMailSettingsFromProfile(
            @PathVariable("userName") @ApiParam(value = "The user name", required = true) String userName) {
        mailSettingsService.removeMailSettingsFromProfile(userName);
        return ResponseEntity.ok().build();
    }

}
