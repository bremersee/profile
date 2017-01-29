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

import org.bremersee.profile.domain.ldap.dao.SambaDomainLdapDao;
import org.bremersee.profile.domain.ldap.entity.SambaDomainLdap;
import org.bremersee.profile.domain.ldap.entity.UserGroupLdap;
import org.bremersee.profile.model.UserGroupDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Christian Bremer
 */
@Component("userGroupLdapMapper")
public class UserGroupLdapMapperImpl extends AbstractLdapMapperImpl implements UserGroupLdapMapper {

    private final SambaDomainLdapDao sambaDomainLdapDao;

    @Autowired
    public UserGroupLdapMapperImpl(SambaDomainLdapDao sambaDomainLdapDao) {
        this.sambaDomainLdapDao = sambaDomainLdapDao;
    }

    @Override
    protected void doInit() {
        // nothing to log
    }

    @Override
    public void mapToDto(UserGroupLdap source, UserGroupDto destination) {
        destination.setDescription(source.getDescription());
        destination.setName(source.getName());
        destination.setGidNumber(source.getGidNumber());
        destination.setSambaGroup(source.getSambaGroupType() != null && source.getSambaSID() != null);
        destination.setSambaGroupType(source.getSambaGroupType());
        destination.setSambaSID(source.getSambaSID());

        int index = source.getSambaSID() == null ? -1 : source.getSambaSID().lastIndexOf('-');
        if (index > 0) {
            SambaDomainLdap sambaDomain = sambaDomainLdapDao.findBySambaSID(source.getSambaSID().substring(0, index));
            if (sambaDomain != null) {
                destination.setSambaDomainName(sambaDomain.getSambaDomainName());
            }
        }
    }

    @Override
    public UserGroupDto mapToDto(UserGroupLdap source) {
        UserGroupDto destination = new UserGroupDto();
        mapToDto(source, destination);
        return destination;
    }

    @Override
    public void updateEntity(UserGroupDto source, UserGroupLdap destination) {
        destination.setDescription(source.getDescription());
        destination.setGidNumber(source.getGidNumber());
        destination.setSambaGroup(source.isSambaGroup());
        destination.setSambaGroupType(source.getSambaGroupType());
        destination.setSambaSID(source.getSambaSID());
    }
}
