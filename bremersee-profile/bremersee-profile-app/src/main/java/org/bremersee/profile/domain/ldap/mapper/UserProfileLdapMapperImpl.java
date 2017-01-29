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

package org.bremersee.profile.domain.ldap.mapper;

import org.bremersee.common.security.crypto.password.PasswordEncoder;
import org.bremersee.profile.domain.ldap.entity.UserProfileLdap;
import org.bremersee.profile.model.AbstractUserDto;
import org.bremersee.profile.model.UserProfileCreateRequestDto;
import org.bremersee.profile.model.UserProfileDto;
import org.bremersee.profile.model.UserRegistrationDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Christian Bremer
 */
@Component("userProfileLdapMapper")
public class UserProfileLdapMapperImpl extends AbstractLdapMapperImpl implements UserProfileLdapMapper {

    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserProfileLdapMapperImpl(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    protected void doInit() {
        // nothing to log
    }

    @Override
    public void mapToDto(UserProfileLdap source, UserProfileDto destination) {
        destination.setUid(source.getUid());
        destination.setGender(source.getGender());
        destination.setTitle(source.getTitle());
        destination.setFirstName(source.getFirstName());
        destination.setLastName(source.getLastName());
        mapDateOfBirth(source, destination);
        destination.setPreferredLocale(source.getPreferredLocale());
        destination.setPreferredTimeZone(source.getPreferredTimeZone());
        destination.setEmail(source.getEmail());
        destination.setMobile(source.getMobile());
        destination.setHomePhone(source.getHomePhone());
        destination.setHomePostalAddress(source.getHomePostalAddress());
        destination.setLocation(source.getLocation());
        destination.setState(source.getState());
        destination.setLabeledURI(source.getLabeledURI());
    }

    private void updateAbstractEntity(AbstractUserDto source, UserProfileLdap destination) {
        // the UID cannot be changed
        //destination.setUid(source.getUid()); // NOSONAR
        destination.setGender(source.getGender());
        destination.setTitle(source.getTitle());
        destination.setFirstName(source.getFirstName());
        destination.setLastName(source.getLastName());
        mapDateOfBirth(source, destination);
        destination.setPreferredLocale(source.getPreferredLocale());
        destination.setPreferredTimeZone(source.getPreferredTimeZone());
        // email address and mobile number must be changed by separate methods,
        // which validate the correct value
        //destination.setEmail(source.getEmail()); // NOSONAR
        //destination.setMobile(source.getMobile()); // NOSONAR
    }

    @SuppressWarnings("Duplicates")
    @Override
    public void updateEntity(UserProfileDto source, UserProfileLdap destination) {
        updateAbstractEntity(source, destination);
        destination.setHomePhone(source.getHomePhone());
        destination.setHomePostalAddress(source.getHomePostalAddress());
        destination.setLocation(source.getLocation());
        destination.setState(source.getState());
        destination.setLabeledURI(source.getLabeledURI());
    }

    @Override
    public UserProfileLdap mapToEntity(UserRegistrationDto source) {
        UserProfileLdap destination = new UserProfileLdap();
        updateAbstractEntity(source, destination);
        destination.setUid(source.getUid());
        destination.setPassword(source.getPassword());
        destination.setSambaLmPassword(source.getSambaLmPassword());
        destination.setSambaNtPassword(source.getSambaNtPassword());
        return destination;
    }

    @Override
    public UserProfileLdap mapToEntity(UserProfileCreateRequestDto source) {
        UserProfileLdap destination = new UserProfileLdap();
        updateAbstractEntity(source, destination);
        destination.setUid(source.getUid());
        destination.setEmail(source.getEmail());
        // there's no mobile attribute
        //destination.setMobile(source.getMobile()); // NOSONAR
        destination.setPassword(passwordEncoder.encode(source.getPassword()));
        destination.setSambaLmPassword(passwordEncoder.createSambaLMPassword(source.getPassword()));
        destination.setSambaNtPassword(passwordEncoder.createSambaNTPassword(source.getPassword()));
        return destination;
    }

    private void mapDateOfBirth(AbstractUserDto source, AbstractUserDto destination) {
        try {
            destination.setDateOfBirth(source.getDateOfBirth());
        } catch (RuntimeException re) {
            log.error(getCurrentUserName() + ": Setting/Getting date of birth failed ("
                    + source.getDateOfBirthString() + "). Removing date of birth from user profile ["
                    + destination.getUid() + "].", re);
            destination.setDateOfBirthString(null);
        }
    }

}
