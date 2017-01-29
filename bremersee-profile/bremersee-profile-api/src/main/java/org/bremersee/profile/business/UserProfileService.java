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

import org.bremersee.pagebuilder.model.Page;
import org.bremersee.pagebuilder.model.PageRequest;
import org.bremersee.profile.model.UserProfileCreateRequestDto;
import org.bremersee.profile.model.UserProfileDto;
import org.bremersee.profile.model.UserRegistrationDto;

/**
 * @author Christian Bremer
 */
public interface UserProfileService {

    Page<UserProfileDto> findAll(PageRequest pageRequest);

    UserProfileDto findByIdentifier(String identifier);

    UserProfileDto findByUserName(String userName);

    UserProfileDto findByUidNumber(long uidNumber);

    UserProfileDto findByEmail(String email);

    boolean existsByIdentifier(String identifier);

    boolean existsByUserName(String userName);

    boolean existsByUidNumber(long uidNumber);

    boolean existsByEmail(String email);

    void deleteByUserName(String userName);


    UserProfileDto create(UserRegistrationDto userRegistration);

    UserProfileDto create(UserProfileCreateRequestDto userProfileCreateRequest);

    UserProfileDto update(String userName, UserProfileDto userProfile);

    //void changeEmail(String userName, String newEmail); // ist doppelt

    //AccessResultDto changeEmailByChangeHash(String changeHash, String remoteHost);

//    /**
//     * Changes the mobile number of an user.
//     *
//     * @param userName  the user name
//     * @param newMobile the new mobile number
//     * @return the validated new mobile number
//     */
//    String changeMobile(String userName, String newMobile); // ist doppelt

    //AccessResultDto changeMobileByChangeHash(String changeHash, String remoteHost);

    void changePassword(String userName, String newPassword, String oldPassword);

    void resetPassword(String userName, String newPassword);

}
