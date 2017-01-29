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

package org.bremersee.profile.business;

import org.bremersee.fac.model.AccessResultDto;
import org.bremersee.pagebuilder.model.Page;
import org.bremersee.pagebuilder.model.PageRequest;
import org.bremersee.profile.model.UserRegistrationDto;
import org.bremersee.profile.model.UserRegistrationRequestDto;

import java.io.Serializable;

/**
 * @author Christian Bremer
 */
public interface UserRegistrationService {

    void processRegistrationRequest(UserRegistrationRequestDto request);

    AccessResultDto processRegistrationValidation(String registrationHash, String remoteHost);


    Page<UserRegistrationDto> findAll(PageRequest pageRequest);

    UserRegistrationDto findByRegistrationHash(String registrationHash);

    UserRegistrationDto findByUserName(String userName);

    UserRegistrationDto findByEmail(String email);

    void deleteById(Serializable id);

}
