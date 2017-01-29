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
import org.bremersee.common.spring.autoconfigure.RestConstants;
import org.bremersee.fac.model.AccessResultDto;
import org.bremersee.pagebuilder.PageBuilderUtils;
import org.bremersee.pagebuilder.model.Page;
import org.bremersee.pagebuilder.model.PageDto;
import org.bremersee.profile.business.ChangeEmailService;
import org.bremersee.profile.business.ChangeMobileService;
import org.bremersee.profile.business.UserProfileService;
import org.bremersee.profile.model.UserProfileCreateRequestDto;
import org.bremersee.profile.model.UserProfileDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

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

    @ApiOperation(value = "Tests whether an user with the specified identifier exists or not.")
    @CrossOrigin
    @RequestMapping(
            path = "/{identifier}/exists",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public BooleanDto existsByIdentifier(
            @ApiParam(value = "The identifier (user name, email or UID number)", required = true) String identifier) {

        return new BooleanDto(userProfileService.existsByIdentifier(identifier));
    }

    @ApiOperation(value = "Deletes an user profile.")
    @CrossOrigin
    @RequestMapping(
            path = "/{userName}",
            method = RequestMethod.DELETE)
    public ResponseEntity<Void> deleteByUserName(
            @PathVariable("userName") @ApiParam(value = "The user name", required = true) String userName) {

        userProfileService.deleteByUserName(userName);
        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "Creates an user profile.")
    @CrossOrigin
    @RequestMapping(
            method = RequestMethod.PUT,
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public UserProfileDto create(
            @RequestBody @ApiParam(value = "The user profile", required = true) UserProfileCreateRequestDto request) {

        return userProfileService.create(request);
    }

    @ApiOperation(value = "Updates an user profile.")
    @CrossOrigin
    @RequestMapping(
            path = "/{userName}",
            method = RequestMethod.PUT,
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public UserProfileDto update(
            @PathVariable("userName") @ApiParam(value = "The user name", required = true) String userName,
            @RequestBody @ApiParam(value = "The user profile", required = true) UserProfileDto userProfile) {

        return userProfileService.update(userName, userProfile);
    }

    @ApiOperation(value = "Changes an email.")
    @CrossOrigin
    @RequestMapping(
            path = "/{userName}/change-email",
            method = RequestMethod.POST)
    public ResponseEntity<Void> changeEmail(
            @PathVariable("userName") @ApiParam(value = "The user name", required = true) String userName,
            @RequestParam(name = "newEmail") @ApiParam(value = "The new email", required = true) String newEmail) {

        changeEmailService.changeEmail(userName, newEmail);
        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "Validates the new email.")
    @CrossOrigin
    @RequestMapping(
            path = "/f/email-validation/{changeHash}",
            method = RequestMethod.POST,
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public AccessResultDto changeEmailByChangeHash(
            @PathVariable("changeHash") @ApiParam(value = "The validation hash", required = true) String changeHash,
            HttpServletRequest request) {

        final String remoteHost = request.getRemoteHost();
        return changeEmailService.changeEmailByChangeHash(changeHash, remoteHost);
    }

    @ApiOperation(value = "Change the mobile number.")
    @CrossOrigin
    @RequestMapping(
            path = "/{userName}/change-mobile",
            method = RequestMethod.POST)
    public ResponseEntity<Void> changeMobile(
            @PathVariable("userName") @ApiParam(value = "The user name", required = true) String userName,
            @RequestParam(name = "newMobile") @ApiParam(value = "The new mobile number", required = true)
                    String newMobile) {

        changeMobileService.changeMobile(userName, newMobile);
        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "Validates the new mobile number.")
    @CrossOrigin
    @RequestMapping(
            path = "/f/mobile-validation/{changeHash}",
            method = RequestMethod.POST,
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public AccessResultDto changeMobileByChangeHash(
            @PathVariable("changeHash") @ApiParam(value = "The validation hash", required = true) String changeHash,
            HttpServletRequest request) {

        final String remoteHost = request.getRemoteHost();
        return changeMobileService.changeMobileByChangeHash(changeHash, remoteHost);
    }

    @ApiOperation(value = "Change the password.")
    @CrossOrigin
    @RequestMapping(
            path = "/{userName}/change-password",
            method = RequestMethod.POST)
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
        return ResponseEntity.ok().build();
    }

//    @ApiOperation(value = "Applies organisation settings to the user profile.")
//    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SYSTEM')")
//    @CrossOrigin
//    @RequestMapping(
//            path = "/{userName}/organisation-settings",
//            method = RequestMethod.PUT,
//            consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
//    public ResponseEntity<Void> applyOrganisationSettingsToProfile(
//            @PathVariable("userName") @ApiParam(value = "The user name", required = true) String userName,
//            @RequestBody @ApiParam(value = "The organisation settings", required = true) OrganisationSettingsDto organisationSettings) {
//
//        userProfileService.applyOrganisationSettingsToProfile(userName, organisationSettings);
//        return ResponseEntity.ok().build();
//    }
//
//    @ApiOperation(value = "Returns the organisation settings of the user profile.")
//    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SYSTEM') or #userName == authentication.name")
//    @CrossOrigin
//    @RequestMapping(
//            path = "/{userName}/organisation-settings",
//            method = RequestMethod.GET,
//            produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
//    public OrganisationSettingsDto findOrganisationSettingsByUid(
//            @PathVariable("userName") @ApiParam(value = "The user name", required = true) String userName) {
//
//        return userProfileService.findOrganisationSettingsByUid(userName);
//    }
//
//    @ApiOperation(value = "Removes the organisation settings of the user profile.")
//    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SYSTEM')")
//    @CrossOrigin
//    @RequestMapping(
//            path = "/{userName}/organisation-settings",
//            method = RequestMethod.DELETE,
//            produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
//    public ResponseEntity<Void> removeOrganisationSettingsFromProfile(
//            @PathVariable("userName") @ApiParam(value = "The user name", required = true) String userName) {
//
//        userProfileService.removeOrganisationSettingsFromProfile(userName);
//        return ResponseEntity.ok().build();
//    }
//
//    @ApiOperation(value = "Applies POSIX settings to the user profile.")
//    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SYSTEM')")
//    @CrossOrigin
//    @RequestMapping(
//            path = "/{userName}/posix-settings",
//            method = RequestMethod.PUT,
//            consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
//    public ResponseEntity<Void> applyPosixSettingsToProfile(
//            @PathVariable("userName") @ApiParam(value = "The user name", required = true) String userName,
//            @RequestBody @ApiParam(value = "The POSIX settings", required = true) PosixSettingsDto posixSettings) {
//
//        userProfileService.applyPosixSettingsToProfile(userName, posixSettings);
//        return ResponseEntity.ok().build();
//    }
//
//    @ApiOperation(value = "Returns the posix settings of the user profile.")
//    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SYSTEM') or #userName == authentication.name")
//    @CrossOrigin
//    @RequestMapping(
//            path = "/{userName}/posix-settings",
//            method = RequestMethod.GET,
//            produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
//    public PosixSettingsDto findPosixSettingsByUid(
//            @PathVariable("userName") @ApiParam(value = "The user name", required = true) String userName) {
//
//        return userProfileService.findPosixSettingsByUid(userName);
//    }
//
//    @ApiOperation(value = "Tests whether the posix settings of the user profile exists or not.")
//    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SYSTEM') or #userName == authentication.name")
//    @CrossOrigin
//    @RequestMapping(
//            path = "/{userName}/posix-settings/exists",
//            method = RequestMethod.GET,
//            produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
//    public BooleanDto posixSettingsExistsByUid(
//            @PathVariable("userName") @ApiParam(value = "The user name", required = true) String userName) {
//
//        return new BooleanDto(userProfileService.posixSettingsExistsByUid(userName));
//    }
//
//    @ApiOperation(value = "Removes the posix settings from the user profile.")
//    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SYSTEM')")
//    @CrossOrigin
//    @RequestMapping(
//            path = "/{userName}/posix-settings",
//            method = RequestMethod.DELETE)
//    public ResponseEntity<Void> removePosixSettingsFromProfile(
//            @PathVariable("userName") @ApiParam(value = "The user name", required = true) String userName) {
//
//        userProfileService.removePosixSettingsFromProfile(userName);
//        return ResponseEntity.ok().build();
//    }
//
//    @ApiOperation(value = "Applies samba settings to the user profile.")
//    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SYSTEM')")
//    @CrossOrigin
//    @RequestMapping(
//            path = "/{userName}/samba-settings",
//            method = RequestMethod.PUT,
//            consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
//    public ResponseEntity<Void> applySambaSettingsToProfile(
//            @PathVariable("userName") @ApiParam(value = "The user name", required = true) String userName,
//            @RequestBody @ApiParam(value = "The samba settings", required = true) SambaSettingsDto sambaSettings) {
//
//        userProfileService.applySambaSettingsToProfile(userName, sambaSettings);
//        return ResponseEntity.ok().build();
//    }
//
//    @ApiOperation(value = "Returns the samba settings of the user profile.")
//    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SYSTEM') or #userName == authentication.name")
//    @CrossOrigin
//    @RequestMapping(
//            path = "/{userName}/samba-settings",
//            method = RequestMethod.GET,
//            produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
//    public SambaSettingsDto findSambaSettingsByUid(
//            @PathVariable("userName") @ApiParam(value = "The user name", required = true) String userName) {
//
//        return userProfileService.findSambaSettingsByUid(userName);
//    }
//
//    @ApiOperation(value = "Tests whether the samba settings of the user profile exists or not.")
//    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SYSTEM') or #userName == authentication.name")
//    @CrossOrigin
//    @RequestMapping(
//            path = "/{userName}/samba-settings/exists",
//            method = RequestMethod.GET,
//            produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
//    public BooleanDto sambaSettingsExistsByUid(
//            @PathVariable("userName") @ApiParam(value = "The user name", required = true) String userName) {
//
//        return new BooleanDto(userProfileService.sambaSettingsExistsByUid(userName));
//    }
//
//    @ApiOperation(value = "Removes the samba settings from the user profile.")
//    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SYSTEM')")
//    @CrossOrigin
//    @RequestMapping(
//            path = "/{userName}/samba-settings",
//            method = RequestMethod.DELETE)
//    public ResponseEntity<Void> removeSambaSettingsFromProfile(
//            @PathVariable("userName") @ApiParam(value = "The user name", required = true) String userName) {
//
//        userProfileService.removeSambaSettingsFromProfile(userName);
//        return ResponseEntity.ok().build();
//    }
//
//    @ApiOperation(value = "Applies mail settings to the user profile.")
//    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SYSTEM')")
//    @CrossOrigin
//    @RequestMapping(
//            path = "/{userName}/mail-settings",
//            method = RequestMethod.PUT,
//            consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
//    public ResponseEntity<Void> applyMailSettingsToProfile(
//            @PathVariable("userName") @ApiParam(value = "The user name", required = true) String userName,
//            @RequestBody @ApiParam(value = "The mail settings", required = true) MailSettingsDto mailSettings) {
//
//        userProfileService.applyMailSettingsToProfile(userName, mailSettings);
//        return ResponseEntity.ok().build();
//    }
//
//    @ApiOperation(value = "Returns the mail settings of the user profile.")
//    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SYSTEM') or #userName == authentication.name")
//    @CrossOrigin
//    @RequestMapping(
//            path = "/{userName}/mail-settings",
//            method = RequestMethod.GET,
//            produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
//    public MailSettingsDto findMailSettingsByUid(
//            @PathVariable("userName") @ApiParam(value = "The user name", required = true) String userName) {
//
//        return userProfileService.findMailSettingsByUid(userName);
//    }
//
//    @ApiOperation(value = "Tests whether the mail settings of the user profile exists or not.")
//    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SYSTEM') or #userName == authentication.name")
//    @CrossOrigin
//    @RequestMapping(
//            path = "/{userName}/mail-settings/exists",
//            method = RequestMethod.GET,
//            produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
//    public BooleanDto mailSettingsExistsByUid(
//            @PathVariable("userName") @ApiParam(value = "The user name", required = true) String userName) {
//
//        return new BooleanDto(userProfileService.mailSettingsExistsByUid(userName));
//    }
//
//    @ApiOperation(value = "Removes the mail settings from the user profile.")
//    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SYSTEM')")
//    @CrossOrigin
//    @RequestMapping(
//            path = "/{userName}/mail-settings",
//            method = RequestMethod.DELETE)
//    public ResponseEntity<Void> removeMailSettingsFromProfile(
//            @PathVariable("userName") @ApiParam(value = "The user name", required = true) String userName) {
//
//        userProfileService.removeMailSettingsFromProfile(userName);
//        return ResponseEntity.ok().build();
//    }

}
