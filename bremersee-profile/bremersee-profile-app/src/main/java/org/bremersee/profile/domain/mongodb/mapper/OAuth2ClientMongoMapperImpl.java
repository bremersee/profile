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

import org.bremersee.profile.domain.ldap.dao.RoleLdapDao;
import org.bremersee.profile.domain.mongodb.entity.OAuth2ClientMongo;
import org.bremersee.profile.model.OAuth2ClientDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Christian Bremer
 */
@Component("oAuth2ClientMongoMapper")
public class OAuth2ClientMongoMapperImpl extends AbstractMongoMapperImpl implements OAuth2ClientMongoMapper {

    private final RoleLdapDao roleLdapDao;

    @Autowired
    public OAuth2ClientMongoMapperImpl(final RoleLdapDao roleLdapDao) {
        this.roleLdapDao = roleLdapDao;
    }

    @Override
    protected void doInit() {
        // nothing to log
    }

    @Override
    public void mapToDto(OAuth2ClientMongo source, OAuth2ClientDto destination) {
        mapMongoToBase(source, destination);
        destination.setAccessTokenValiditySeconds(source.getAccessTokenValiditySeconds());
        destination.setAuthorizedGrantTypes(source.getAuthorizedGrantTypes());
        destination.setAutoApproveScopes(source.getAutoApproveScopes());
        destination.setClientId(source.getClientId());
        destination.setRefreshTokenValiditySeconds(source.getRefreshTokenValiditySeconds());
        destination.setRegisteredRedirectUri(source.getRegisteredRedirectUri());
        destination.setResourceIds(source.getResourceIds());
        destination.setRoles(roleLdapDao.findRoleNamesByMember(source.getClientId()));
        destination.setScope(source.getScope());
        destination.setAdditionalInformation(source.getAdditionalInformation());
    }

    @Override
    public OAuth2ClientDto mapToDto(OAuth2ClientMongo source) {
        OAuth2ClientDto destination = new OAuth2ClientDto();
        mapToDto(source, destination);
        return destination;
    }

    @Override
    public void updateEntity(OAuth2ClientDto source, OAuth2ClientMongo destination) {
        mapBaseToMongo(source, destination);
        destination.setAccessTokenValiditySeconds(source.getAccessTokenValiditySeconds());
        destination.setAuthorizedGrantTypes(source.getAuthorizedGrantTypes());
        destination.setAutoApproveScopes(source.getAutoApproveScopes());
        //destination.setClientId(source.getClientId()); // NOSONAR
        destination.setRefreshTokenValiditySeconds(source.getRefreshTokenValiditySeconds());
        destination.setRegisteredRedirectUri(source.getRegisteredRedirectUri());
        destination.setResourceIds(source.getResourceIds());
        //destination.setRoles(source.getRoles()); // NOSONAR
        destination.setScope(source.getScope());
        destination.setAdditionalInformation(source.getAdditionalInformation());
        //destination.setClientSecret(); // NOSONAR
    }

}
