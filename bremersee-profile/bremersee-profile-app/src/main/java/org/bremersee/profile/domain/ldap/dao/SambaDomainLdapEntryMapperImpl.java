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

package org.bremersee.profile.domain.ldap.dao;

import org.bremersee.profile.domain.ldap.entity.SambaDomainLdap;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Christian Bremer
 */
@Component("sambaDomainLdapEntryMapper")
@EnableConfigurationProperties(SambaDomainLdapProperties.class)
public class SambaDomainLdapEntryMapperImpl extends AbstractLdapEntryMapper implements SambaDomainLdapEntryMapper {

    private SambaDomainLdapProperties properties = new SambaDomainLdapProperties();

    @Autowired(required = false)
    public void setProperties(SambaDomainLdapProperties properties) {
        this.properties = properties;
    }

    @Override
    void doInit() {
        log.info("properties = {}", properties);
    }

    @Override
    public String createDn(final String sambaDomainName) {
        return createDn(sambaDomainName, properties);
    }

    @Override
    public String mapDn(final SambaDomainLdap object) {
        return createDn(object == null ? null : object.getSambaDomainName());
    }

    @Override
    public void map(SambaDomainLdap source, LdapEntry dest) { // NOSONAR

        dest.setDn(createDn(source.getSambaDomainName()));
        if (source.getUidNumber() != null && source.getGidNumber() != null) {
            dest.addAttribute(new LdapAttribute("objectClass", "top", "sambaDomain", "sambaUnixIdPool"));
            dest.addAttribute(new LdapAttribute("uidNumber", source.getUidNumber().toString()));
            dest.addAttribute(new LdapAttribute("gidNumber", source.getGidNumber().toString())); // NOSONAR
        } else {
            dest.addAttribute(new LdapAttribute("objectClass", "top", "sambaDomain"));
            dest.removeAttribute("uidNumber");
            dest.removeAttribute("gidNumber");
        }

        dest.addAttribute(new LdapAttribute("sambaDomainName", source.getSambaDomainName()));
        dest.addAttribute(new LdapAttribute("sambaSID", source.getSambaSID()));

        if (source.getSambaAlgorithmicRidBase() != null) {
            dest.addAttribute(new LdapAttribute("sambaAlgorithmicRidBase", // NOSONAR
                    source.getSambaAlgorithmicRidBase().toString()));
        } else {
            dest.addAttribute(new LdapAttribute("sambaAlgorithmicRidBase", "1000"));
        }
        if (source.getSambaForceLogoff() != null) {
            dest.addAttribute(new LdapAttribute("sambaForceLogoff", // NOSONAR
                    source.getSambaForceLogoff().toString()));
        } else {
            dest.addAttribute(new LdapAttribute("sambaForceLogoff", "-1"));
        }
        if (source.getSambaLockoutDuration() != null) {
            dest.addAttribute(new LdapAttribute("sambaLockoutDuration", // NOSONAR
                    source.getSambaLockoutDuration().toString()));
        } else {
            dest.addAttribute(new LdapAttribute("sambaLockoutDuration", "30"));
        }
        if (source.getSambaLockoutObservationWindow() != null) {
            dest.addAttribute(new LdapAttribute("sambaLockoutObservationWindow", // NOSONAR
                    source.getSambaLockoutObservationWindow().toString()));
        } else {
            dest.addAttribute(new LdapAttribute("sambaLockoutObservationWindow", "30"));
        }
        if (source.getSambaLockoutThreshold() != null) {
            dest.addAttribute(new LdapAttribute("sambaLockoutThreshold", // NOSONAR
                    source.getSambaLockoutThreshold().toString()));
        } else {
            dest.addAttribute(new LdapAttribute("sambaLockoutThreshold", "0"));
        }
        if (source.getSambaLogonToChgPwd() != null) {
            dest.addAttribute(new LdapAttribute("sambaLogonToChgPwd", // NOSONAR
                    source.getSambaLogonToChgPwd().toString()));
        } else {
            dest.addAttribute(new LdapAttribute("sambaLogonToChgPwd", "0"));
        }
        if (source.getSambaMaxPwdAge() != null) {
            dest.addAttribute(new LdapAttribute("sambaMaxPwdAge", // NOSONAR
                    source.getSambaMaxPwdAge().toString()));
        } else {
            dest.addAttribute(new LdapAttribute("sambaMaxPwdAge", "-1"));
        }
        if (source.getSambaMinPwdAge() != null) {
            dest.addAttribute(new LdapAttribute("sambaMinPwdAge", // NOSONAR
                    source.getSambaMinPwdAge().toString()));
        } else {
            dest.addAttribute(new LdapAttribute("sambaMinPwdAge", "0"));
        }
        if (source.getSambaMinPwdLength() != null) {
            dest.addAttribute(new LdapAttribute("sambaMinPwdLength", // NOSONAR
                    source.getSambaMinPwdLength().toString()));
        } else {
            dest.addAttribute(new LdapAttribute("sambaMinPwdLength", "0"));
        }
        if (source.getSambaNextUserRid() != null) {
            dest.addAttribute(new LdapAttribute("sambaNextUserRid", // NOSONAR
                    source.getSambaNextUserRid().toString()));
        } else {
            dest.addAttribute(new LdapAttribute("sambaNextUserRid", "1000"));
        }
        if (source.getSambaPwdHistoryLength() != null) {
            dest.addAttribute(new LdapAttribute("sambaPwdHistoryLength", // NOSONAR
                    source.getSambaPwdHistoryLength().toString()));
        } else {
            dest.addAttribute(new LdapAttribute("sambaPwdHistoryLength", "0"));
        }
        if (source.getSambaRefuseMachinePwdChange() != null) {
            dest.addAttribute(new LdapAttribute("sambaRefuseMachinePwdChange", // NOSONAR
                    source.getSambaRefuseMachinePwdChange().toString()));
        } else {
            dest.addAttribute(new LdapAttribute("sambaRefuseMachinePwdChange", "0"));
        }
    }

    @Override
    public void map(LdapEntry source, SambaDomainLdap dest) {

        dest.setGidNumber(getLong(source, "gidNumber", null));
        dest.setSambaAlgorithmicRidBase(getLong(source, "sambaAlgorithmicRidBase", 1000L));
        dest.setSambaDomainName(getString(source, "sambaDomainName", null));
        dest.setSambaForceLogoff(getLong(source, "sambaForceLogoff", -1L).intValue());
        dest.setSambaLockoutDuration(getLong(source, "sambaLockoutDuration", 30L).intValue());
        dest.setSambaLockoutObservationWindow(getLong(source, "sambaLockoutObservationWindow", 30L).intValue());
        dest.setSambaLockoutThreshold(getLong(source, "sambaLockoutThreshold", 0L).intValue());
        dest.setSambaLogonToChgPwd(getLong(source, "sambaLogonToChgPwd", 0L).intValue());
        dest.setSambaMaxPwdAge(getLong(source, "sambaMaxPwdAge", -1L).intValue());
        dest.setSambaMinPwdAge(getLong(source, "sambaMinPwdAge", 0L).intValue());
        dest.setSambaMinPwdLength(getLong(source, "sambaMinPwdLength", 0L).intValue());
        dest.setSambaNextUserRid(getLong(source, "sambaNextUserRid", 1000L));
        dest.setSambaPwdHistoryLength(getLong(source, "sambaPwdHistoryLength", 0L).intValue());
        dest.setSambaRefuseMachinePwdChange(getLong(source, "sambaRefuseMachinePwdChange", 0L).intValue());
    }

    @Override
    public SambaDomainLdap toEntity(final LdapEntry source) {
        SambaDomainLdap destination = new SambaDomainLdap();
        map(source, destination);
        return destination;
    }

}
