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

package org.bremersee.profile.business;

import org.bremersee.profile.domain.ldap.dao.RoleLdapDao;
import org.bremersee.profile.domain.mongodb.entity.OAuth2ClientMongo;
import org.bremersee.profile.domain.mongodb.repository.OAuth2ClientMongoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationException;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Christian Bremer
 */
@SuppressWarnings("unused")
@Service("oAuth2ClientDetailsService")
public class OAuth2ClientDetailsServiceImpl implements ClientDetailsService {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final RoleLdapDao roleLdapDao;

    private final OAuth2ClientMongoRepository oAuth2ClientMongoRepository;

    @Autowired
    public OAuth2ClientDetailsServiceImpl(
            RoleLdapDao roleLdapDao,
            OAuth2ClientMongoRepository oAuth2ClientMongoRepository) {

        this.roleLdapDao = roleLdapDao;
        this.oAuth2ClientMongoRepository = oAuth2ClientMongoRepository;
    }

    @Override
    public ClientDetails loadClientByClientId(final String clientId) {

        OAuth2ClientMongo entity = oAuth2ClientMongoRepository.findByClientId(clientId);
        if (entity == null) {
            ClientRegistrationException e = new ClientRegistrationException(
                    String.format("OAuth client [%s] was not found.", clientId)); // NOSONAR
            log.error("Loading client failed.", e);
            throw e;
        }
        final Set<String> roleNames = roleLdapDao.findRoleNamesByMember(entity.getClientId());
        final List<GrantedAuthority> authorities = roleNames.stream().map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        BaseClientDetails clientDetails = new BaseClientDetails();
        clientDetails.setAccessTokenValiditySeconds(entity.getAccessTokenValiditySeconds());
        clientDetails.setAdditionalInformation(entity.getAdditionalInformation());
        clientDetails.setAuthorities(authorities);
        clientDetails.setAuthorizedGrantTypes(entity.getAuthorizedGrantTypes());
        clientDetails.setAutoApproveScopes(entity.getAutoApproveScopes());
        clientDetails.setClientId(entity.getClientId());
        clientDetails.setClientSecret(entity.getClientSecret());
        clientDetails.setRefreshTokenValiditySeconds(entity.getRefreshTokenValiditySeconds());
        clientDetails.setRegisteredRedirectUri(entity.getRegisteredRedirectUri());
        clientDetails.setResourceIds(entity.getResourceIds());
        clientDetails.setScope(entity.getScope());
        return clientDetails;
    }

}
