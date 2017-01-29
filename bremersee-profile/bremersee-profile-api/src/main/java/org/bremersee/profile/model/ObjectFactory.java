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

package org.bremersee.profile.model;

import javax.xml.bind.annotation.XmlRegistry;

/**
 * @author Christian Bremer
 *
 */
@SuppressWarnings("unused")
@XmlRegistry
public class ObjectFactory {
    
    public MailChangeRequestDto createMailChangeRequestDto() {
        return new MailChangeRequestDto();
    }
    
    public MailSettingsDto createMailSettingsDto() {
        return new MailSettingsDto();
    }
    
    public MobileChangeRequestDto createMobileChangeRequestDto() {
        return new MobileChangeRequestDto();
    }

    public OAuth2ClientCreateRequestDto createOAuth2ClientCreateRequestDto() {
        return new OAuth2ClientCreateRequestDto();
    }

    public OAuth2ClientDto createOAuth2ClientDto() {
        return new OAuth2ClientDto();
    }
    
    public OrganisationSettingsDto createOrganisationSettingsDto() {
        return new OrganisationSettingsDto();
    }
    
    public PosixSettingsDto createPosixSettingsDto() {
        return new PosixSettingsDto();
    }
    
    public RoleDto createRoleDto() {
        return new RoleDto();
    }
    
    public SambaDomainDto createSambaDomainDto() {
        return new SambaDomainDto();
    }
    
    public SambaSettingsDto createSambaSettingsDto() {
        return new SambaSettingsDto();
    }
    
    public UserGroupDto createUserGroupDto() {
        return new UserGroupDto();
    }
    
    public UserProfileCreateRequestDto createUserProfileCreateRequestDto() {
        return new UserProfileCreateRequestDto();
    }
    
    public UserProfileDto createUserProfileDto() {
        return new UserProfileDto();
    }
    
    public UserRegistrationDto createUserRegistrationDto() {
        return new UserRegistrationDto();
    }
    
    public UserRegistrationRequestDto createUserRegistrationRequestDto() {
        return new UserRegistrationRequestDto();
    }

}
