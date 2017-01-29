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

package org.bremersee.profile.domain.ldap.entity;

import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.bremersee.profile.model.*;

/**
 * @author Christian Bremer
 */
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class UserProfileLdap extends UserProfileDto {

    private static final long serialVersionUID = 1L;

    private String password;

    private String sambaLmPassword;

    private String sambaNtPassword;

    private MailSettingsDto mailSettings;

    private OrganisationSettingsDto organisationSettings;

    private PosixSettingsDto posixSettings;

    private SambaSettingsDto sambaSettings;

    public boolean hasMailSettings() {
        return mailSettings != null && StringUtils.isNotBlank(mailSettings.getGosaMailServer());
    }

    public boolean hasOrganisationSettings() {
        return organisationSettings != null && !organisationSettings.areAllValuesEmpty();
    }

    public boolean hasPosixSettings() {
        return posixSettings != null && posixSettings.getUidNumber() != null;
    }

    public boolean hasSambaSettings() {
        return sambaSettings != null && StringUtils.isNotBlank(sambaSettings.getSambaAcctFlags());
    }

    public String getDisplayName() {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotBlank(getFirstName())) {
            sb.append(getFirstName());
        }
        if (StringUtils.isNotBlank(getLastName())) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(getLastName());
        }
        if (sb.length() > 0) {
            return sb.toString();
        }
        return null;
    }

    @SuppressWarnings("unused")
    public void setDisplayName(final String displayName) {
        // ignored
    }

}
