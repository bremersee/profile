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

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.bremersee.common.spring.autoconfigure.RestConstants;
import org.bremersee.fac.model.AccessResultDto;
import org.bremersee.pagebuilder.PageBuilderUtils;
import org.bremersee.pagebuilder.model.Page;
import org.bremersee.pagebuilder.model.PageDto;
import org.bremersee.profile.business.UserRegistrationService;
import org.bremersee.profile.model.UserRegistrationDto;
import org.bremersee.profile.model.UserRegistrationRequestDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Christian Bremer
 */
@RestController
@RequestMapping(path = RestConstants.REST_CONTEXT_PATH + "/user-registration")
public class UserRegistrationRestController extends AbstractRestControllerImpl {

    private UserRegistrationService userRegistrationService;

    @Autowired
    public UserRegistrationRestController(final UserRegistrationService userRegistrationService) {
        this.userRegistrationService = userRegistrationService;
    }

    @Override
    protected void doInit() {
        // nothing to init
    }

    @ApiOperation(value = "Processes an user registration requests.")
    @CrossOrigin
    @RequestMapping(method = RequestMethod.PUT,
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<Void> processRegistrationRequest(
            @RequestBody @ApiParam(value = "The request", required = true) UserRegistrationRequestDto request) {

        userRegistrationService.processRegistrationRequest(request);
        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "Processes an user registration validation.")
    @CrossOrigin
    @RequestMapping(path = "/validation/{registrationHash}",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public AccessResultDto processRegistrationValidation(
            @PathVariable("registrationHash") @ApiParam(value = "The registration hash", required = true) String registrationHash,
            HttpServletRequest request) {

        String remoteHost = request.getRemoteHost();
        return userRegistrationService.processRegistrationValidation(registrationHash, remoteHost);
    }


    @ApiOperation("Finds all user registration requests.")
    @CrossOrigin
    @RequestMapping(
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public PageDto findAll(
            @RequestParam(name = "p", required = false) @ApiParam("The page number.") Integer pageNumber,
            @RequestParam(name = "s", required = false) @ApiParam("The page size.") Integer pageSize,
            @RequestParam(name = "c", required = false) @ApiParam("The comparator item or chain.") String comparatorItem,
            @RequestParam(name = "q", required = false) @ApiParam("A query value.") String query) {

        Page<UserRegistrationDto> page = userRegistrationService.findAll(
                createPageRequest(pageNumber, pageSize, comparatorItem, query));
        return PageBuilderUtils.createPageDto(page, null);
    }

    @ApiOperation("Finds an user registration requests by hash.")
    @CrossOrigin
    @RequestMapping(
            path = "/f/find-by-hash",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public UserRegistrationDto findByRegistrationHash(
            @RequestParam(name = "hash") @ApiParam(value = "The registration hash", required = true) String registrationHash) {

        return userRegistrationService.findByRegistrationHash(registrationHash);
    }

    @ApiOperation("Finds an user registration requests by user.")
    @CrossOrigin
    @RequestMapping(
            path = "/f/find-by-user",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public UserRegistrationDto findByUserName(
            @RequestParam(name = "user") @ApiParam(value = "The user name", required = true) String userName) {

        return userRegistrationService.findByUserName(userName);
    }

    @ApiOperation("Finds an user registration requests by email.")
    @CrossOrigin
    @RequestMapping(
            path = "/f/find-by-email",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public UserRegistrationDto findByEmail(
            @RequestParam(name = "email") @ApiParam(value = "The email", required = true) String email) {

        return userRegistrationService.findByEmail(email);
    }

    @ApiOperation("Deletes an user registration requests.")
    @CrossOrigin
    @RequestMapping(
            path = "/{id}",
            method = RequestMethod.DELETE)
    public ResponseEntity<Void> deleteById(
            @PathVariable("id") @ApiParam(value = "The database ID", required = true) String id) {

        userRegistrationService.deleteById(id);
        return ResponseEntity.ok().build();
    }

}
