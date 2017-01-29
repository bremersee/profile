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

import org.bremersee.profile.domain.ldap.entity.RoleLdap;
import org.bremersee.profile.model.RoleDto;
import org.springframework.stereotype.Component;

/**
 * @author Christian Bremer
 */
@Component("roleLdapMapper")
public class RoleLdapMapperImpl extends AbstractLdapMapperImpl implements RoleLdapMapper {

    @Override
    protected void doInit() {
        // nothing to log
    }

    @Override
    public void mapToDto(RoleLdap source, RoleDto destination) {
        destination.setDescription(source.getDescription());
        destination.setName(source.getName());
    }

    @Override
    public RoleDto mapToDto(RoleLdap source) {
        RoleDto destination = new RoleDto();
        mapToDto(source, destination);
        return destination;
    }

    @Override
    public void updateEntity(RoleDto source, RoleLdap destination) {
        // the name cannot be changed
        //destination.setName(source.getName()); // NOSONAR
        destination.setDescription(source.getDescription());
    }

}
