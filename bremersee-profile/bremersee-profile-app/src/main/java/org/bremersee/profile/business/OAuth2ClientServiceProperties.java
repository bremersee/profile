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
import org.bremersee.profile.model.OAuth2ClientCreateRequestDto;
import org.bremersee.profile.model.RoleDto;
import org.bremersee.utils.PasswordUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Christian Bremer
 */
@ConfigurationProperties("profile.business.client-service")
@Data
public class OAuth2ClientServiceProperties {

    private OAuth2ClientCreateRequestDto systemClient = new OAuth2ClientCreateRequestDto();

    private OAuth2ClientCreateRequestDto swaggerUiClient = new OAuth2ClientCreateRequestDto();

    private List<OAuth2ClientCreateRequestDto> otherClients = new ArrayList<>();

    public OAuth2ClientServiceProperties() {

        systemClient.setClientId("system");
        systemClient.setClientSecret("secret4SYSTEM");
        systemClient.getAuthorizedGrantTypes().add("authorization_code");
        systemClient.getAuthorizedGrantTypes().add("client_credentials");
        systemClient.getAuthorizedGrantTypes().add("refresh_token");
        systemClient.getAuthorizedGrantTypes().add("password");
        systemClient.getAuthorizedGrantTypes().add("implicit");
        systemClient.getScope().add("openid");
        systemClient.getScope().add("profile");
        systemClient.getAutoApproveScopes().add(Boolean.TRUE.toString());
        systemClient.getRoles().add(RoleDto.SYSTEM_ROLE_NAME);
        systemClient.getRoles().add(RoleDto.ACL_ADMIN_ROLE_NAME);

        swaggerUiClient.setClientId("swaggerui");
        swaggerUiClient.setClientSecret(PasswordUtils.createRandomClearPassword(20, true, true));
        swaggerUiClient.getAuthorizedGrantTypes().add("implicit");
        swaggerUiClient.getScope().add("openid");
        swaggerUiClient.getScope().add("profile");
        swaggerUiClient.getAutoApproveScopes().add(Boolean.TRUE.toString());
    }

}
