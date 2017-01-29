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
import org.bremersee.profile.domain.ldap.entity.UserGroupLdap;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * @author Christian Bremer
 */
@Component("userGroupLdapEntryMapper")
@EnableConfigurationProperties(UserGroupLdapProperties.class)
public class UserGroupLdapEntryMapperImpl extends AbstractLdapEntryMapper implements UserGroupLdapEntryMapper {

    private UserGroupLdapProperties properties = new UserGroupLdapProperties();

    @Autowired(required = false)
    public void setProperties(UserGroupLdapProperties properties) {
        this.properties = properties;
    }

    @Override
    void doInit() {
        log.info("properties = {}", properties);
    }

    @Override
    public String createDn(final String groupName) {
        return createDn(groupName, properties);
    }

    @Override
    public String mapDn(UserGroupLdap object) {
        return createDn(object == null ? null : object.getName());
    }

    @Override
    public void map(UserGroupLdap source, LdapEntry dest) {
        dest.setDn(createDn(source.getName()));
        List<String> objectClasses = Arrays.asList("top", "posixGroup", "groupOfNames");
        dest.addAttribute(new LdapAttribute("cn", source.getName()));
        if (StringUtils.isNotBlank(source.getDescription())) {
            dest.addAttribute(new LdapAttribute("description", source.getDescription())); // NOSONAR
        } else {
            dest.removeAttribute("description");
        }
        if (source.getGidNumber() != null) {
            dest.addAttribute(new LdapAttribute("gidNumber", source.getGidNumber().toString())); // NOSONAR
        } else {
            dest.removeAttribute("gidNumber");
        }
        if (source.isSambaGroup()) {
            objectClasses.add("sambaGroupMapping");
            dest.addAttribute(new LdapAttribute("sambaGroupType", // NOSONAR
                    source.getSambaGroupType().toString()));
            dest.addAttribute(new LdapAttribute("sambaSID", source.getSambaSID())); // NOSONAR
        } else {
            dest.removeAttribute("sambaGroupType");
            dest.removeAttribute("sambaSID");
        }
        String[] members = new String[source.getMembers().size()];
        int i = 0;
        for (String member : source.getMembers()) {
            members[i] = createMemberDn(member);
            i++;
        }
        if (members.length > 0) {
            dest.addAttribute(new LdapAttribute("member", members)); // NOSONAR
            dest.addAttribute(new LdapAttribute("memberUid", // NOSONAR
                    source.getMembers().toArray(new String[source.getMembers().size()])));
        } else if (StringUtils.isNotBlank(properties.getDefaultMember())) {
            dest.addAttribute(new LdapAttribute("member", createMemberDn(properties.getDefaultMember())));
            dest.addAttribute(new LdapAttribute("memberUid", properties.getDefaultMember()));
        } else {
            dest.addAttribute(new LdapAttribute("member", mapDn(source)));
            dest.removeAttribute("memberUid");
        }
        dest.addAttribute(new LdapAttribute("objectClass",
                objectClasses.toArray(new String[objectClasses.size()])));
    }

    @Override
    public void map(LdapEntry source, UserGroupLdap dest) {
        dest.setName(getString(source, "cn", null));
        dest.setDescription(getString(source, "description", null));
        String gidNumber = getString(source, "gidNumber", null);
        if (gidNumber != null) {
            dest.setGidNumber(Long.parseLong(gidNumber));
        }
        String sambaGroupType = getString(source, "sambaGroupType", null);
        if (sambaGroupType != null) {
            dest.setSambaGroupType(Integer.parseInt(sambaGroupType));
        }
        dest.setSambaSID(getString(source, "sambaSID", null));
        for (String member : getStringList(source, "memberUid")) {
            dest.getMembers().add(member);
        }
    }

    @Override
    public UserGroupLdap toEntity(LdapEntry source) {
        UserGroupLdap destination = new UserGroupLdap();
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
