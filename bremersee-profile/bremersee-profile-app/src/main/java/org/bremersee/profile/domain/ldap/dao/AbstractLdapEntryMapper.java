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
import org.apache.commons.lang3.Validate;
import org.bremersee.profile.domain.ldap.LdapEntryUtils;
import org.ldaptive.LdapEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * @author Christian Bremer
 */
public abstract class AbstractLdapEntryMapper {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    @PostConstruct
    public void init() {
        log.info("Initializing {} ...", getClass().getSimpleName());
        doInit();
        log.info("{} successfully initialized.", getClass().getSimpleName());
    }

    abstract void doInit();

    String createDn(final String rdnValue, final AbstractLdapProperties properties) {
        String dn = LdapEntryUtils.createDn(rdnValue, properties);
        log.debug("Create DN (rdnValue = {}, baseDn = {}): {}",
                rdnValue, properties.getSearchRequest().getBaseDn(), dn);
        return dn;
    }

    Long getLong(final LdapEntry entry, final String attribute, final Long nullValue) {
        String value = getString(entry, attribute, null);
        if (value == null) {
            return nullValue;
        }
        try {
            return Long.parseLong(value);
        } catch (Exception e) {
            log.error("Parsing number [" + value + "] failed. Returning default value [" + nullValue + "].", e);
            return nullValue;
        }
    }

    String getString(final LdapEntry entry, final String attribute, final String nullValue) {
        return LdapEntryUtils.getString(entry, attribute, nullValue);
    }

    List<String> getStringList(final LdapEntry entry, final String attribute) {
        return LdapEntryUtils.getStringList(entry, attribute);
    }

    String createMemberDn(final String uid, final String memberRdn, final String memberBaseDn) {
        Validate.notBlank(uid, "Member UID must not be null or blank.");
        Validate.notBlank(memberRdn, "Member RDN must not be null or blank.");
        int start = uid.indexOf('=');
        int end = uid.indexOf(',');
        if (start >= 0 && start < end) {
            return uid;
        }
        return memberRdn + "=" + uid + (StringUtils.isNotBlank(memberBaseDn) ? "," + memberBaseDn : "");
    }

    String createMemberUid(final String member) {
        Validate.notBlank(member, "Member must not be null or blank.");
        int start = member.indexOf('=');
        int end = member.indexOf(',');
        if (start >= 0 && start < end) {
            return member.substring(start + 1, end);
        }
        return member;
    }

}
