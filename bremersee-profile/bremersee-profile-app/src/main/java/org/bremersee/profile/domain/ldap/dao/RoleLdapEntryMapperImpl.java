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

package org.bremersee.profile.domain.ldap.dao;

import org.apache.commons.lang3.StringUtils;
import org.bremersee.profile.domain.ldap.entity.RoleLdap;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Christian Bremer
 */
@Component("roleLdapEntryMapper")
@EnableConfigurationProperties(RoleLdapProperties.class)
public class RoleLdapEntryMapperImpl extends AbstractLdapEntryMapper implements RoleLdapEntryMapper {

    private RoleLdapProperties properties = new RoleLdapProperties();

    @Autowired(required = false)
    public void setProperties(RoleLdapProperties properties) {
        this.properties = properties;
    }

    @Override
    void doInit() {
        log.info("properties = {}", properties);
    }

    @Override
    public String createDn(String roleName) {
        return createDn(roleName, properties);
    }

    @Override
    public String mapDn(RoleLdap object) {
        return createDn(object == null ? null : object.getName());
    }

    @Override
    public void map(RoleLdap source, LdapEntry dest) {
        dest.setDn(createDn(source.getName()));
        dest.addAttribute(new LdapAttribute("objectClass", "top", "organizationalRole"));
        dest.addAttribute(new LdapAttribute("cn", source.getName()));
        if (StringUtils.isNotBlank(source.getDescription())) {
            dest.addAttribute(new LdapAttribute("description", source.getDescription())); // NOSONAR
        } else {
            dest.removeAttribute("description");
        }
        String[] members = new String[source.getMembers().size()];
        int i = 0;
        for (String member : source.getMembers()) {
            members[i] = createMemberDn(member);
            i++;
        }
        if (members.length > 0) {
            dest.addAttribute(new LdapAttribute("roleOccupant", members)); // NOSONAR
        } else if (StringUtils.isNotBlank(properties.getDefaultMember())) {
            dest.addAttribute(new LdapAttribute("roleOccupant", createMemberDn(properties.getDefaultMember())));
        } else {
            dest.removeAttribute("roleOccupant");
        }
    }

    @Override
    public void map(LdapEntry source, RoleLdap dest) {
        dest.setName(getString(source, "cn", null));
        dest.setDescription(getString(source, "description", null));
        for (String memberDn : getStringList(source, "roleOccupant")) {
            dest.getMembers().add(createUid(memberDn));
        }
    }

    @Override
    public RoleLdap toEntity(LdapEntry source) {
        RoleLdap destination = new RoleLdap();
        map(source, destination);
        return destination;
    }

    @Override
    public String createMemberDn(String uid) {
        return createMemberDn(uid, properties.getMemberRdn(), properties.getMemberBaseDn());
    }

    @Override
    public String createUid(String member) {
        return createMemberUid(member);
    }

}
