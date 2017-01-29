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

package org.bremersee.profile.domain.mongodb.mapper;

import org.bremersee.profile.domain.mongodb.entity.UserProfileMongo;
import org.bremersee.profile.model.AbstractUserDto;
import org.bremersee.profile.model.UserProfileCreateRequestDto;
import org.bremersee.profile.model.UserProfileDto;
import org.bremersee.profile.model.UserRegistrationDto;
import org.springframework.stereotype.Component;

/**
 * @author Christian Bremer
 */
@Component
public class UserProfileMongoMapperImpl extends AbstractMongoMapperImpl implements UserProfileMongoMapper {

    @Override
    protected void doInit() {
        // nothing to init
    }

    @Override
    public UserProfileDto mapToDto(UserProfileMongo source) {
        UserProfileDto destination = new UserProfileDto();
        mapToDto(source, destination);
        return destination;
    }

    @Override
    public void mapToDto(UserProfileMongo source, UserProfileDto destination) {
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
        destination.setMobile(source.getMobile());
        destination.setHomePhone(source.getHomePhone());
        destination.setHomePostalAddress(source.getHomePostalAddress());
        destination.setLocation(source.getLocation());
        destination.setState(source.getState());
        destination.setLabeledURI(source.getLabeledURI());
    }

    private void updateAbstractEntity(AbstractUserDto source, UserProfileMongo destination) {
        mapBaseToMongo(source, destination);
        // the UID cannot be changed
        //destination.setUid(source.getUid()); // NOSONAR
        destination.setGender(source.getGender());
        destination.setTitle(source.getTitle());
        destination.setFirstName(source.getFirstName());
        destination.setLastName(source.getLastName());
        destination.setDateOfBirth(source.getDateOfBirthString());
        destination.setPreferredLocale(source.getPreferredLocaleString());
        destination.setPreferredTimeZoneID(source.getPreferredTimeZoneID());
        // email address and mobile number must be changed by separate methods,
        // which validate the correct value
        //destination.setEmail(source.getEmail()); // NOSONAR
        //destination.setMobile(source.getMobile()); // NOSONAR
    }

    @SuppressWarnings("Duplicates")
    @Override
    public void updateEntity(UserProfileDto source, UserProfileMongo destination) {
        updateAbstractEntity(source, destination);
        destination.setHomePhone(source.getHomePhone());
        destination.setHomePostalAddress(source.getHomePostalAddress());
        destination.setLocation(source.getLocation());
        destination.setState(source.getState());
        destination.setLabeledURI(source.getLabeledURI());
    }

    @Override
    public UserProfileMongo mapToEntity(UserRegistrationDto source) {
        UserProfileMongo destination = new UserProfileMongo();
        updateAbstractEntity(source, destination);
        destination.setUid(source.getUid());
        //destination.setPassword(source.getPassword()); // NOSONAR
        //destination.setSambaLmPassword(source.getSambaLmPassword()); // NOSONAR
        //destination.setSambaNtPassword(source.getSambaNtPassword()); // NOSONAR
        return destination;
    }

    @Override
    public UserProfileMongo mapToEntity(UserProfileCreateRequestDto source) {

        UserProfileMongo destination = new UserProfileMongo();
        updateAbstractEntity(source, destination);
        destination.setUid(source.getUid());
        destination.setEmail(source.getEmail());
        // there's no mobile attribute
        //destination.setMobile(source.getMobile()); // NOSONAR
        return destination;
    }

}
