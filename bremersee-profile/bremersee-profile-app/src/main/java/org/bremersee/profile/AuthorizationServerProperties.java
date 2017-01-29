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

package org.bremersee.profile;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Christian Bremer
 */
@ConfigurationProperties("profile.authorization-server")
@Data
@NoArgsConstructor
public class AuthorizationServerProperties {

    private String jwtKeyStoreLocation = "classpath:/jwt.jks";

    private String jwtKeyStorePassword = "changeit"; // NOSONAR

    private String jwtKeyPairAlias = "jwt";

    private String jwtKeyPairPassword = null;

    private String realm = "oauth2/client";

    private boolean allowFormAuthenticationForClients = false;

    private String tokenKeyAccess = "permitAll()"; // original: denyAll()

    private String checkTokenAccess = "isAuthenticated()"; // original: denyAll()

    private boolean sslOnly = false;

}
