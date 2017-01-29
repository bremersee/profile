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

import lombok.Data;
import org.bremersee.profile.model.RoleDto;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Christian Bremer
 */
@ConfigurationProperties("profile.business.user-profile")
@Data
@SuppressWarnings({"MismatchedQueryAndUpdateOfCollection", "WeakerAccess"})
public class UserProfileProperties {

    private boolean checkAllProfilesAtStartup = true;

    private String adminPassword = "secret4ADMIN"; // NOSONAR

    private List<String> defaultUserRoles = new ArrayList<>();

    private String userRegistrationSender = "no-reply@bremersee.org";

    private String userRegistrationSubject = "Welcome to bremersee.org";

    private String userRegistrationSubjectCode = "userRegistrationService.sendValidationEmail.subject";

    private String userRegistrationLink = "https://bremersee.org/profile/user-registration/validation/{registrationHash}";

    private long userRegistrationLifetimeInDays = 30L;

    private String changeEmailSender = "no-reply@bremersee.org";

    private String changeEmailSubject = "Confirm your new email";

    private String changeEmailSubjectCode = "userProfileService.sendValidationEmail.subject";

    private String changeEmailLink = "https://bremersee.org/profile/email/validation/{changeHash}";

    private String changeMobileSmsLink = "https://bremersee.org/profile/mobile/validation/{changeHash}";

    private long changeEmailLifetimeInDays = 30L;

    private long changeMobileLifetimeInDays = 3L;

    private String linkEncoding = "UTF-8";

    private int minQueryLengthForUnprivilegedUsers = 3;

    public UserProfileProperties() {
        defaultUserRoles.add(RoleDto.USER_ROLE_NAME);
    }

    public Duration getUserRegistrationLifetimeDuration() {
        return Duration.ofDays(userRegistrationLifetimeInDays);
    }

    public Duration getChangeEmailLifetimeDuration() {
        return Duration.ofDays(changeEmailLifetimeInDays);
    }

    public Duration getChangeMobileLifetimeDuration() {
        return Duration.ofDays(changeMobileLifetimeInDays);
    }

}
