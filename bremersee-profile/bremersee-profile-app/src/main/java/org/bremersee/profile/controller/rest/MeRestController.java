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

import io.swagger.annotations.Api;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import org.bremersee.profile.SwaggerConfig;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

/**
 * @author Christian Bremer
 */
@RestController
@RequestMapping(path = "/api/me")
public class MeRestController extends AbstractRestControllerImpl {

    @Override
    protected void doInit() {
        // nothing to init
    }

    @PreAuthorize(HAS_OAUTH2_SCOPE_OPENID)
    @RequestMapping
    public Principal me(Principal me) {
        return me;
    }

}
