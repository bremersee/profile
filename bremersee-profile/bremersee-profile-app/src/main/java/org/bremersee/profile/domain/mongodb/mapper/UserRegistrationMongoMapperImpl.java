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

import org.bremersee.common.security.crypto.password.PasswordEncoder;
import org.bremersee.profile.domain.mongodb.entity.UserRegistrationMongo;
import org.bremersee.profile.model.UserRegistrationDto;
import org.bremersee.profile.model.UserRegistrationRequestDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Christian Bremer
 */
@Component("userRegistrationMongoMapper")
public class UserRegistrationMongoMapperImpl extends AbstractMongoMapperImpl implements UserRegistrationMongoMapper {

    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserRegistrationMongoMapperImpl(final PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    protected void doInit() {
        // nothing to init
    }

    @Override
    public UserRegistrationDto mapToDto(UserRegistrationMongo source) {
        UserRegistrationDto destination = new UserRegistrationDto();
        mapToDto(source, destination);
        return destination;
    }

    @Override
    public void mapToDto(UserRegistrationMongo source, UserRegistrationDto destination) {
        mapMongoToBase(source, destination);
        destination.setUid(source.getUid());
        destination.setGender(source.getGender());
        destination.setTitle(source.getTitle());
        destination.setFirstName(source.getFirstName());
        destination.setLastName(source.getLastName());
        destination.setDateOfBirthString(source.getDateOfBirth());
        destination.setPreferredLocaleString(source.getPreferredLocale());
        destination.setPreferredTimeZoneID(source.getPreferredTimeZoneID());
        destination.setEmail(source.getEmail());

        destination.setRegistrationExpiration(source.getRegistrationExpiration());
        destination.setRegistrationHash(source.getRegistrationHash());

        // we don not map passwords
//        destination.setPassword(source.getPassword());                 // NOSONAR
//        destination.setSambaLmPassword(source.getSambaLmPassword());   // NOSONAR
//        destination.setSambaNtPassword(source.getSambaNtPassword());   // NOSONAR
    }

    @Override
    public UserRegistrationMongo mapToEntity(UserRegistrationRequestDto source) {

        UserRegistrationMongo destination = new UserRegistrationMongo();

        destination.setUid(source.getUid());
        destination.setGender(source.getGender());
        destination.setTitle(source.getTitle());
        destination.setFirstName(source.getFirstName());
        destination.setLastName(source.getLastName());
        mapDateOfBirth(source, destination);
        destination.setPreferredLocale(source.getPreferredLocale().toLanguageTag());
        destination.setPreferredTimeZoneID(source.getPreferredTimeZone().getID());
        destination.setEmail(source.getEmail());

        destination.setPassword(passwordEncoder.encode(source.getPassword()));
        destination.setSambaLmPassword(passwordEncoder.createSambaLMPassword(source.getPassword()));
        destination.setSambaNtPassword(passwordEncoder.createSambaNTPassword(source.getPassword()));

        return destination;
    }

    private void mapDateOfBirth(UserRegistrationRequestDto source, UserRegistrationMongo destination) {
        try {
            if (source.getDateOfBirth() != null) {
                destination.setDateOfBirth(source.getDateOfBirthString());
            } else {
                destination.setDateOfBirth(null);
            }
        } catch (RuntimeException re) {
            log.error(getCurrentUserName() + ": Setting/Getting date of birth failed ("
                    + source.getDateOfBirthString() + "). Removing date of birth from user registration ["
                    + destination.getUid() + "].", re);
            destination.setDateOfBirth(null);
        }
    }

}
