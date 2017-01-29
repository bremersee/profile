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

package org.bremersee.profile;

import lombok.Data;
import org.bremersee.profile.model.UserProfileDto;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Christian Bremer
 */
@SuppressWarnings({"WeakerAccess", "MismatchedQueryAndUpdateOfCollection"})
@ConfigurationProperties("profile.common")
@Data
public class AbstractComponentProperties {

    private String systemName;

    private UserProfileDto adminProfile;

    private Set<String> adminRoleNames;

    private Set<String> systemRoleNames;

    public AbstractComponentProperties() {
        systemName = "system";
        adminProfile = new UserProfileDto();
        adminProfile.setUid("admin");
        adminProfile.setFirstName("Super");
        adminProfile.setLastName("User");
        adminProfile.setEmail("admin@example.org");
        adminRoleNames = new HashSet<>();
        systemRoleNames = new HashSet<>();
    }

}
