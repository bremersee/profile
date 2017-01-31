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
import org.bremersee.fac.model.AccessResultDto;
import org.bremersee.pagebuilder.PageBuilderUtils;
import org.bremersee.pagebuilder.model.Page;
import org.bremersee.pagebuilder.model.PageDto;
import org.bremersee.profile.SwaggerConfig;
import org.bremersee.profile.business.ChangeEmailService;
import org.bremersee.profile.business.ChangeMobileService;
import org.bremersee.profile.business.UserProfileService;
import org.bremersee.profile.model.UserProfileCreateRequestDto;
import org.bremersee.profile.model.UserProfileDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;

/**
 * @author Christian Bremer
 */
@RestController
@RequestMapping(path = "/api/user-profile")
public class UserProfileRestController extends AbstractRestControllerImpl {

    private final UserProfileService userProfileService;

    private final ChangeEmailService changeEmailService;

    private final ChangeMobileService changeMobileService;

    @Autowired
    public UserProfileRestController(final UserProfileService userProfileService,
                                     final ChangeEmailService changeEmailService,
                                     final ChangeMobileService changeMobileService) {
        this.userProfileService = userProfileService;
        this.changeEmailService = changeEmailService;
        this.changeMobileService = changeMobileService;
    }

    @Override
    protected void doInit() {
        // nothing to init
    }

    @ApiOperation(value = "Finds all user profiles.")
    @CrossOrigin
    @RequestMapping(
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public PageDto findAll(
            @RequestParam(name = "p", required = false) @ApiParam("The page number.") Integer pageNumber,
            @RequestParam(name = "s", required = false) @ApiParam("The page size.") Integer pageSize,
            @RequestParam(name = "c", required = false) @ApiParam("The comparator item or chain.") String comparatorItem,
            @RequestParam(name = "q", required = false) @ApiParam("A query value.") String query) {

        Page<UserProfileDto> page = userProfileService
                .findAll(createPageRequest(pageNumber, pageSize, comparatorItem, query));
        return PageBuilderUtils.createPageDto(page, null);
    }

    @ApiOperation(value = "Find user profile by identifier.")
    @CrossOrigin
    @RequestMapping(
            path = "/{identifier}",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public UserProfileDto findByIdentifier(
            @PathVariable("identifier")
            @ApiParam(value = "The identifier (user name, email or UID number)", required = true) String identifier) {

        return userProfileService.findByIdentifier(identifier);
    }

    @ApiOperation(value = "Test whether an user with the specified identifier exists or not.")
    @CrossOrigin
    @RequestMapping(
            path = "/{identifier}/exists",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public BooleanDto existsByIdentifier(
            @ApiParam(value = "The identifier (user name, email or UID number)", required = true) String identifier) {

        return new BooleanDto(userProfileService.existsByIdentifier(identifier));
    }

    @ApiOperation(value = "Delete an user profile.")
    @CrossOrigin
    @RequestMapping(
            path = "/{userName}",
            method = RequestMethod.DELETE)
    public ResponseEntity<Void> deleteByUserName(
            @PathVariable("userName") @ApiParam(value = "The user name", required = true) String userName) {

        userProfileService.deleteByUserName(userName);
        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "Create an user profile.")
    @CrossOrigin
    @RequestMapping(
            method = RequestMethod.POST,
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<UserProfileDto> create(
            @RequestBody @ApiParam(value = "The user profile", required = true) UserProfileCreateRequestDto request) {

        UserProfileDto dto = userProfileService.create(request);
        final URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{userGroupName}")
                .buildAndExpand(dto.getUid()).toUri();
        return ResponseEntity.created(location).body(dto);
    }

    @ApiOperation(value = "Update an user profile.")
    @CrossOrigin
    @RequestMapping(
            path = "/{userName}",
            method = RequestMethod.PATCH,
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> update(
            @PathVariable("userName") @ApiParam(value = "The user name", required = true) String userName,
            @RequestBody @ApiParam(value = "The user profile", required = true) UserProfileDto userProfile) {

        userProfileService.update(userName, userProfile);
        return ResponseEntity.noContent().build();
    }

    @ApiOperation(value = "Change the email address.")
    @CrossOrigin
    @RequestMapping(
            path = "/{userName}/change-email",
            method = RequestMethod.PATCH)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> changeEmail(
            @PathVariable("userName") @ApiParam(value = "The user name", required = true) String userName,
            @RequestParam(name = "newEmail") @ApiParam(value = "The new email", required = true) String newEmail) {

        changeEmailService.changeEmail(userName, newEmail);
        return ResponseEntity.noContent().build();
    }

    @ApiOperation(value = "Validate the new email address.")
    @CrossOrigin
    @RequestMapping(
            path = "/f/email-validation/{changeHash}",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<AccessResultDto> changeEmailByChangeHash(
            @PathVariable("changeHash") @ApiParam(value = "The validation hash", required = true) String changeHash,
            HttpServletRequest request) {

        final String remoteHost = request.getRemoteHost();
        AccessResultDto dto = changeEmailService.changeEmailByChangeHash(changeHash, remoteHost);
        return ResponseEntity.ok(dto);
    }

    @ApiOperation(value = "Change the mobile number.")
    @CrossOrigin
    @RequestMapping(
            path = "/{userName}/change-mobile",
            method = RequestMethod.PATCH)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> changeMobile(
            @PathVariable("userName") @ApiParam(value = "The user name", required = true) String userName,
            @RequestParam(name = "newMobile") @ApiParam(value = "The new mobile number", required = true)
                    String newMobile) {

        changeMobileService.changeMobile(userName, newMobile);
        return ResponseEntity.noContent().build();
    }

    @ApiOperation(value = "Validates the new mobile number.")
    @CrossOrigin
    @RequestMapping(
            path = "/f/mobile-validation/{changeHash}",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<AccessResultDto> changeMobileByChangeHash(
            @PathVariable("changeHash") @ApiParam(value = "The validation hash", required = true) String changeHash,
            HttpServletRequest request) {

        final String remoteHost = request.getRemoteHost();
        AccessResultDto dto = changeMobileService.changeMobileByChangeHash(changeHash, remoteHost);
        return ResponseEntity.ok(dto);
    }

    @ApiOperation(value = "Change the password.")
    @CrossOrigin
    @RequestMapping(
            path = "/{userName}/change-password",
            method = RequestMethod.PATCH)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> changePassword(
            @PathVariable("userName") @ApiParam(value = "The user name", required = true) String userName,
            @RequestParam("newPassword") @ApiParam(value = "The new password", required = true) String newPassword,
            @RequestParam(name = "oldPassword", required = false)
            @ApiParam(value = "The old password") String oldPassword) {

        if (StringUtils.isBlank(oldPassword)) {
            userProfileService.resetPassword(userName, newPassword);
        } else {
            userProfileService.changePassword(userName, newPassword, oldPassword);
        }
        return ResponseEntity.noContent().build();
    }

}
