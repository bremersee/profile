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

package org.bremersee.profile.domain.mongodb.mapper;

import org.bremersee.profile.domain.mongodb.entity.UserRegistrationMongo;
import org.bremersee.profile.model.UserRegistrationDto;
import org.bremersee.profile.model.UserRegistrationRequestDto;

/**
 * @author Christian Bremer
 */
public interface UserRegistrationMongoMapper {

    UserRegistrationDto mapToDto(UserRegistrationMongo source);

    void mapToDto(UserRegistrationMongo source, UserRegistrationDto destination);

    UserRegistrationMongo mapToEntity(UserRegistrationRequestDto source);

}
