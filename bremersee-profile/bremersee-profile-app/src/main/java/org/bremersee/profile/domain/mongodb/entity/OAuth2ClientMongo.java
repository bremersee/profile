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

package org.bremersee.profile.domain.mongodb.entity;

import lombok.*;
import org.bremersee.common.domain.mongodb.entity.AbstractBaseMongo;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Christian Bremer
 *
 */
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Document(collection = "oauth2Client")
public class OAuth2ClientMongo extends AbstractBaseMongo {
    
    private static final long serialVersionUID = 1L;

    @Indexed(unique = true)
    private String clientId;
    
    private Set<String> resourceIds = new LinkedHashSet<>();
    
    private String clientSecret;
    
    private Set<String> scope = new LinkedHashSet<>();
    
    private Set<String> authorizedGrantTypes = new LinkedHashSet<>();
    
    private Set<String> registeredRedirectUri = new LinkedHashSet<>();
    
    //private Set<String> roles = new LinkedHashSet<>();
    
    private Integer accessTokenValiditySeconds;
    
    private Integer refreshTokenValiditySeconds;
    
    private Set<String> autoApproveScopes = new LinkedHashSet<>();

    private Map<String, Object> additionalInformation = new LinkedHashMap<>();

}
