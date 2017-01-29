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
import org.bremersee.common.model.Gender;
import org.bremersee.profile.domain.ldap.entity.UserProfileLdap;
import org.bremersee.profile.model.MailSettingsDto;
import org.bremersee.profile.model.OrganisationSettingsDto;
import org.bremersee.profile.model.PosixSettingsDto;
import org.bremersee.profile.model.SambaSettingsDto;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * @author Christian Bremer
 */
@Component("userProfileLdapEntryMapper")
@EnableConfigurationProperties(UserProfileLdapProperties.class)
public class UserProfileLdapEntryMapperImpl extends AbstractLdapEntryMapper implements UserProfileLdapEntryMapper {

    private static final String[] PROFILE_OBJECT_CLASSES = {
            "top",
            "person",
            "organizationalPerson",
            "inetOrgPerson", // structural
            "gosaAccount"
    };

    private static final String[] POSIX_OBJECT_CLASSES = {
            "posixAccount",
            "shadowAccount"
    };

    private static final String[] SAMBA_OBJECT_CLASSES = {
            "sambaSamAccount"
    };

    private static final String[] MAIL_OBJECT_CLASSES = {
            "gosaMailAccount"
    };

    private UserProfileLdapProperties properties = new UserProfileLdapProperties();

    @Autowired(required = false)
    public void setProperties(UserProfileLdapProperties properties) {
        this.properties = properties;
    }

    @Override
    void doInit() {
        log.info("properties = {}", properties);
    }

    @Override
    public String createDn(final String uid) {
        return createDn(uid, properties);
    }

    @Override
    public String mapDn(final UserProfileLdap object) {
        return createDn(object == null ? null : object.getUid());
    }

    @Override
    public UserProfileLdap toEntity(LdapEntry source) {
        UserProfileLdap destination = new UserProfileLdap();
        map(source, destination);
        return destination;
    }

    @Override
    public void map(UserProfileLdap source, LdapEntry dest) { // NOSONAR

        List<String> objectClasses = new ArrayList<>();
        objectClasses.addAll(Arrays.asList(PROFILE_OBJECT_CLASSES));

        dest.setDn(mapDn(source));

        dest.addAttribute(new LdapAttribute("cn", source.getDisplayName()));

        if (source.getDateOfBirth() != null) {
            dest.addAttribute(new LdapAttribute("dateOfBirth", source.getDateOfBirthString())); // NOSONAR
        } else {
            dest.removeAttribute("dateOfBirth");
        }

        if (StringUtils.isNotBlank(source.getEmail())) {
            dest.addAttribute(new LdapAttribute("mail", source.getEmail()));
        } else {
            dest.removeAttribute("mail");
        }

        if (source.getGender() != null) {
            dest.addAttribute(new LdapAttribute("gender", source.getGender().getGosaValue())); // NOSONAR
        } else {
            dest.removeAttribute("gender");
        }

        if (StringUtils.isNotBlank(source.getFirstName())) {
            dest.addAttribute(new LdapAttribute("givenName", source.getFirstName())); // NOSONAR
        } else {
            dest.removeAttribute("givenName");
        }

        if (StringUtils.isNotBlank(source.getHomePhone())) {
            dest.addAttribute(new LdapAttribute("homePhone", source.getHomePhone())); // NOSONAR
        } else {
            dest.removeAttribute("homePhone");
        }

        if (StringUtils.isNotBlank(source.getHomePostalAddress())) {
            dest.addAttribute(new LdapAttribute("homePostalAddress", source.getHomePostalAddress())); // NOSONAR
        } else {
            dest.removeAttribute("homePostalAddress");
        }

        // jpegPhoto

        if (StringUtils.isNotBlank(source.getLocation())) {
            dest.addAttribute(new LdapAttribute("l", source.getLocation()));
        } else {
            dest.removeAttribute("l");
        }

        if (StringUtils.isNotBlank(source.getLabeledURI())) {
            dest.addAttribute(new LdapAttribute("labeledURI", source.getLabeledURI())); // NOSONAR
        } else {
            dest.removeAttribute("labeledURI");
        }

        if (StringUtils.isNotBlank(source.getMobile())) {
            dest.addAttribute(new LdapAttribute("mobile", source.getMobile())); // NOSONAR
        } else {
            dest.removeAttribute("mobile");
        }

        if (StringUtils.isNotBlank(source.getPassword())) {
            dest.addAttribute(new LdapAttribute("userPassword", source.getPassword())); // NOSONAR
        } else {
            dest.removeAttribute("userPassword");
        }

        if (StringUtils.isNotBlank(source.getTitle())) {
            dest.addAttribute(new LdapAttribute("personalTitle", source.getTitle())); // NOSONAR
        } else {
            dest.removeAttribute("personalTitle");
        }

        if (source.getPreferredLocale() != null) {
            dest.addAttribute(new LdapAttribute("preferredLanguage", // NOSONAR
                    source.getPreferredLocaleString()));
        } else {
            dest.removeAttribute("preferredLanguage");
        }

        dest.addAttribute(new LdapAttribute("sn", source.getLastName()));

        if (StringUtils.isNotBlank(source.getState())) {
            dest.addAttribute(new LdapAttribute("st", source.getState()));
        } else {
            dest.removeAttribute("st");
        }

        dest.addAttribute(new LdapAttribute("uid", source.getUid()));


        if (source.hasOrganisationSettings()) {
            if (StringUtils.isNotBlank(source.getOrganisationSettings().getDepartmentNumber())) {
                dest.addAttribute(new LdapAttribute("departmentNumber", // NOSONAR
                        source.getOrganisationSettings().getDepartmentNumber()));
            } else {
                dest.removeAttribute("departmentNumber");
            }

            if (StringUtils.isNotBlank(source.getOrganisationSettings().getEmployeeType())) {
                dest.addAttribute(new LdapAttribute("employeeNumber",  // NOSONAR
                        source.getOrganisationSettings().getEmployeeNumber()));
            } else {
                dest.removeAttribute("employeeNumber");
            }

            if (StringUtils.isNotBlank(source.getOrganisationSettings().getEmployeeType())) {
                dest.addAttribute(new LdapAttribute("employeeType", // NOSONAR
                        source.getOrganisationSettings().getEmployeeType()));
            } else {
                dest.removeAttribute("employeeType");
            }

            if (StringUtils.isNotBlank(source.getOrganisationSettings().getFacsimileTelephoneNumber())) {
                dest.addAttribute(new LdapAttribute("facsimileTelephoneNumber", // NOSONAR
                        source.getOrganisationSettings().getFacsimileTelephoneNumber()));
            } else {
                dest.removeAttribute("facsimileTelephoneNumber");
            }

//            if (StringUtils.isNotBlank(source.getOrganisationSettings().getManager())) {  // NOSONAR
//            } else {
//                dest.removeAttribute("manager");
//            }

            if (StringUtils.isNotBlank(source.getOrganisationSettings().getOrganisation())) {
                dest.addAttribute(new LdapAttribute("o", source.getOrganisationSettings().getOrganisation()));
            } else {
                dest.removeAttribute("o");
            }

            if (StringUtils.isNotBlank(source.getOrganisationSettings().getOrganisationUnit())) {
                dest.addAttribute(new LdapAttribute("ou",
                        source.getOrganisationSettings().getOrganisationUnit()));
            } else {
                dest.removeAttribute("ou");
            }

            if (StringUtils.isNotBlank(source.getOrganisationSettings().getPager())) {
                dest.addAttribute(new LdapAttribute("pager", // NOSONAR
                        source.getOrganisationSettings().getPager()));
            } else {
                dest.removeAttribute("pager");
            }

            if (StringUtils.isNotBlank(source.getOrganisationSettings().getPostalAddress())) {
                dest.addAttribute(new LdapAttribute("postalAddress", // NOSONAR
                        source.getOrganisationSettings().getPostalAddress()));
            } else {
                dest.removeAttribute("postalAddress");
            }

            if (StringUtils.isNotBlank(source.getOrganisationSettings().getRoomNumber())) {
                dest.addAttribute(new LdapAttribute("roomNumber", // NOSONAR
                        source.getOrganisationSettings().getRoomNumber()));
            } else {
                dest.removeAttribute("roomNumber");
            }

            if (StringUtils.isNotBlank(source.getOrganisationSettings().getTelephoneNumber())) {
                dest.addAttribute(new LdapAttribute("telephoneNumber", // NOSONAR
                        source.getOrganisationSettings().getTelephoneNumber()));
            } else {
                dest.removeAttribute("telephoneNumber");
            }

        } else {

            dest.removeAttribute("departmentNumber");
            dest.removeAttribute("employeeNumber");
            dest.removeAttribute("employeeType");
            dest.removeAttribute("facsimileTelephoneNumber");
            dest.removeAttribute("manager");
            dest.removeAttribute("o");
            dest.removeAttribute("ou");
            dest.removeAttribute("pager");
            dest.removeAttribute("postalAddress");
            dest.removeAttribute("roomNumber");
            dest.removeAttribute("telephoneNumber");
        }


        if (source.hasPosixSettings()) {

            objectClasses.addAll(Arrays.asList(POSIX_OBJECT_CLASSES));

            dest.addAttribute(new LdapAttribute("uidNumber", // NOSONAR
                    source.getPosixSettings().getUidNumber().toString()));
            dest.addAttribute(new LdapAttribute("gidNumber", // NOSONAR
                    source.getPosixSettings().getGidNumber().toString()));
            dest.addAttribute(new LdapAttribute("homeDirectory", // NOSONAR
                    source.getPosixSettings().getHomeDirectory()));
            dest.addAttribute(new LdapAttribute("loginShell", // NOSONAR
                    source.getPosixSettings().getLoginShell()));

            if (StringUtils.isNotBlank(source.getPosixSettings().getGecos())) {
                dest.addAttribute(new LdapAttribute("gecos", // NOSONAR
                        source.getPosixSettings().getGecos()));
            } else {
                dest.removeAttribute("gecos");
            }

        } else {

            dest.removeAttribute("uidNumber");
            dest.removeAttribute("gidNumber");
            dest.removeAttribute("homeDirectory");
            dest.removeAttribute("loginShell");
            dest.removeAttribute("gecos");
        }


        if (source.getSambaSettings() == null) {
            source.setSambaSettings(new SambaSettingsDto());
        }

        if (source.getSambaSettings().getSambaBadPasswordCount() != null) {
            dest.addAttribute(new LdapAttribute("sambaBadPasswordCount", // NOSONAR
                    source.getSambaSettings().getSambaBadPasswordCount().toString()));
        } else {
            dest.addAttribute(new LdapAttribute("sambaBadPasswordCount", "0"));
        }

        if (source.getSambaSettings().getSambaBadPasswordTime() != null) {
            dest.addAttribute(new LdapAttribute("sambaBadPasswordTime", // NOSONAR
                    source.getSambaSettings().getSambaBadPasswordTime().toString()));
        } else {
            dest.addAttribute(new LdapAttribute("sambaBadPasswordTime", "0"));
        }

        if (StringUtils.isNotBlank(source.getSambaLmPassword())) {
            dest.addAttribute(new LdapAttribute("sambaLMPassword", // NOSONAR
                    source.getSambaLmPassword()));
        } else {
            dest.removeAttribute("sambaLMPassword");
        }

        if (StringUtils.isNotBlank(source.getSambaNtPassword())) {
            dest.addAttribute(new LdapAttribute("sambaNTPassword", // NOSONAR
                    source.getSambaNtPassword()));
        } else {
            dest.removeAttribute("sambaNTPassword");
        }

        if (source.getSambaSettings().getSambaPwdLastSet() != null) {
            dest.addAttribute(new LdapAttribute("sambaPwdLastSet", // NOSONAR
                    source.getSambaSettings().getSambaPwdLastSet().toString()));
        } else {
            String v = Long.toString(System.currentTimeMillis() / 1000L);
            dest.addAttribute(new LdapAttribute("sambaPwdLastSet", v));
        }

        if (source.hasSambaSettings()) {

            objectClasses.addAll(Arrays.asList(SAMBA_OBJECT_CLASSES));

            dest.addAttribute(new LdapAttribute("sambaAcctFlags", // NOSONAR
                    source.getSambaSettings().getSambaAcctFlags()));

            if (StringUtils.isNotBlank(source.getSambaSettings().getSambaDomainName())) {
                dest.addAttribute(new LdapAttribute("sambaDomainName", // NOSONAR
                        source.getSambaSettings().getSambaDomainName()));
            } else {
                dest.removeAttribute("sambaDomainName");
            }
            if (source.getSambaSettings().getSambaLogoffTime() != null) {
                dest.addAttribute(new LdapAttribute("sambaLogoffTime", // NOSONAR
                        source.getSambaSettings().getSambaLogoffTime().toString()));
            } else {
                dest.removeAttribute("sambaLogoffTime");
            }
            if (source.getSambaSettings().getSambaLogonTime() != null) {
                dest.addAttribute(new LdapAttribute("sambaLogonTime", // NOSONAR
                        source.getSambaSettings().getSambaLogonTime().toString()));
            } else {
                dest.removeAttribute("sambaLogonTime");
            }
            if (StringUtils.isNotBlank(source.getSambaSettings().getSambaMungedDial())) {
                dest.addAttribute(new LdapAttribute("sambaMungedDial", // NOSONAR
                        source.getSambaSettings().getSambaMungedDial()));
            } else {
                dest.removeAttribute("sambaMungedDial");
            }
            if (StringUtils.isNotBlank(source.getSambaSettings().getSambaPrimaryGroupSID())) {
                dest.addAttribute(new LdapAttribute("sambaPrimaryGroupSID", // NOSONAR
                        source.getSambaSettings().getSambaPrimaryGroupSID()));
            } else {
                dest.removeAttribute("sambaPrimaryGroupSID");
            }
            if (StringUtils.isNotBlank(source.getSambaSettings().getSambaSID())) {
                dest.addAttribute(new LdapAttribute("sambaSID", // NOSONAR
                        source.getSambaSettings().getSambaSID()));
            } else {
                dest.removeAttribute("sambaSID");
            }

        } else {

            dest.removeAttribute("sambaAcctFlags");
            dest.removeAttribute("sambaDomainName");
            dest.removeAttribute("sambaLogoffTime");
            dest.removeAttribute("sambaLogonTime");
            dest.removeAttribute("sambaMungedDial");
            dest.removeAttribute("sambaPrimaryGroupSID");
            dest.removeAttribute("sambaSID");
        }


        if (source.hasMailSettings()) {

            objectClasses.addAll(Arrays.asList(MAIL_OBJECT_CLASSES));

            if (!source.getMailSettings().getGosaMailAlternateAddresses().isEmpty()) {
                dest.addAttribute(new LdapAttribute("gosaMailAlternateAddress", // NOSONAR
                        source.getMailSettings().getGosaMailAlternateAddresses().toArray(
                                new String[source.getMailSettings().getGosaMailAlternateAddresses().size()])));
            } else {
                dest.removeAttribute("gosaMailAlternateAddress");
            }
            if (StringUtils.isNotBlank(source.getMailSettings().getGosaMailDeliveryMode())) {
                dest.addAttribute(new LdapAttribute("gosaMailDeliveryMode", // NOSONAR
                        source.getMailSettings().getGosaMailDeliveryMode()));
            } else {
                dest.removeAttribute("gosaMailDeliveryMode");
            }
            if (!source.getMailSettings().getGosaMailForwardingAddresses().isEmpty()) {
                dest.addAttribute(new LdapAttribute("gosaMailForwardingAddress", // NOSONAR
                        source.getMailSettings().getGosaMailForwardingAddresses().toArray(
                                new String[source.getMailSettings().getGosaMailForwardingAddresses().size()])));
            } else {
                dest.removeAttribute("gosaMailForwardingAddress");
            }
            if (StringUtils.isNotBlank(source.getMailSettings().getGosaMailQuota())) {
                dest.addAttribute(new LdapAttribute("gosaMailQuota", // NOSONAR
                        source.getMailSettings().getGosaMailQuota()));
            } else {
                dest.removeAttribute("gosaMailQuota");
            }
            dest.addAttribute(new LdapAttribute("gosaMailServer", // NOSONAR
                    source.getMailSettings().getGosaMailServer()));
            if (StringUtils.isNotBlank(source.getMailSettings().getGosaSpamMailbox())) {
                dest.addAttribute(new LdapAttribute("gosaSpamMailbox", // NOSONAR
                        source.getMailSettings().getGosaSpamMailbox()));
            } else {
                dest.removeAttribute("gosaSpamMailbox");
            }
            if (StringUtils.isNotBlank(source.getMailSettings().getGosaSpamSortLevel())) {
                dest.addAttribute(new LdapAttribute("gosaSpamSortLevel", // NOSONAR
                        source.getMailSettings().getGosaSpamSortLevel()));
            } else {
                dest.removeAttribute("gosaSpamSortLevel");
            }

        } else {

            dest.removeAttribute("gosaMailAlternateAddress");
            dest.removeAttribute("gosaMailDeliveryMode");
            dest.removeAttribute("gosaMailForwardingAddress");
            dest.removeAttribute("gosaMailQuota");
            dest.removeAttribute("gosaMailServer");
            dest.removeAttribute("gosaSpamMailbox");
            dest.removeAttribute("gosaSpamSortLevel");
        }

        dest.addAttribute(new LdapAttribute("objectClass",
                objectClasses.toArray(new String[objectClasses.size()])));
    }

    @Override
    public void map(LdapEntry source, UserProfileLdap dest) {

        dest.setDateOfBirthString(getString(source, "dateOfBirth", null));
        dest.setEmail(getString(source, "mail", null));
        dest.setFirstName(getString(source, "givenName", null));
        dest.setGender(Gender.fromGosaValue(getString(source, "gender", null)));
        dest.setHomePhone(getString(source, "homePhone", null));
        dest.setHomePostalAddress(getString(source, "homePostalAddress", null));
        dest.setLabeledURI(getString(source, "labeledURI", null));
        dest.setLastName(getString(source, "sn", null));
        dest.setLocation(getString(source, "l", null));
        dest.setMobile(getString(source, "mobile", null));
        dest.setPassword(getString(source, "userPassword", null));
        dest.setPreferredLocaleString(getString(source, "preferredLanguage", Locale.getDefault().toString()));
        dest.setState(getString(source, "st", null));
        dest.setTitle(getString(source, "personalTitle", null));
        dest.setUid(getString(source, "uid", null));

        // Organisation
        OrganisationSettingsDto org = new OrganisationSettingsDto();
        org.setDepartmentNumber(getString(source, "departmentNumber", null));
        org.setEmployeeNumber(getString(source, "employeeNumber", null));
        org.setEmployeeType(getString(source, "employeeType", null));
        org.setFacsimileTelephoneNumber(getString(source, "facsimileTelephoneNumber", null));
        //org.setManager(manager); // NOSONAR
        org.setOrganisation(getString(source, "o", null));
        org.setOrganisationUnit(getString(source, "ou", null));
        org.setPager(getString(source, "pager", null));
        org.setPostalAddress(getString(source, "postalAddress", null));
        org.setRoomNumber(getString(source, "roomNumber", null));
        org.setTelephoneNumber(getString(source, "telephoneNumber", null));
        if (org.areAllValuesEmpty()) {
            dest.setOrganisationSettings(null);
        } else {
            dest.setOrganisationSettings(org);
        }

        // POSIX
        String uidNumber = getString(source, "uidNumber", null);
        String gidNumber = getString(source, "gidNumber", null);
        if (StringUtils.isNoneBlank(uidNumber, gidNumber)) {
            if (dest.getPosixSettings() == null) {
                dest.setPosixSettings(new PosixSettingsDto());
            }
            dest.getPosixSettings().setUidNumber(uidNumber == null ? null : Long.parseLong(uidNumber));
            dest.getPosixSettings().setGecos(getString(source, "gecos", null));
            dest.getPosixSettings().setGidNumber(gidNumber == null ? null : Long.parseLong(gidNumber));
            dest.getPosixSettings().setHomeDirectory(getString(source, "homeDirectory", null));
            dest.getPosixSettings().setLoginShell(getString(source, "loginShell", null));
        }

        // Samba
        if (dest.getSambaSettings() == null) {
            dest.setSambaSettings(new SambaSettingsDto());
        }
        dest.getSambaSettings().setSambaBadPasswordCount(Integer.parseInt(getString(source,
                "sambaBadPasswordCount", "0")));
        dest.getSambaSettings().setSambaBadPasswordTime(Integer.parseInt(getString(source,
                "sambaBadPasswordTime", "0")));
        dest.setSambaLmPassword(getString(source, "sambaLMPassword", null));
        dest.setSambaNtPassword(getString(source, "sambaNTPassword", null));
        dest.getSambaSettings().setSambaPwdLastSet(Integer.parseInt(getString(source,
                "sambaPwdLastSet", Long.toString(System.currentTimeMillis() / 1000L)))); // NOSONAR

        dest.getSambaSettings().setSambaAcctFlags(getString(source, "sambaAcctFlags", null));
        dest.getSambaSettings().setSambaDomainName(getString(source, "sambaDomainName", null));
        String sambaLogoffTime = getString(source, "sambaLogoffTime", null);
        dest.getSambaSettings().setSambaLogoffTime(sambaLogoffTime == null ? null : Integer.parseInt(sambaLogoffTime));
        String sambaLogonTime = getString(source, "sambaLogonTime", null);
        dest.getSambaSettings().setSambaLogonTime(sambaLogonTime == null ? null : Integer.parseInt(sambaLogonTime));
        dest.getSambaSettings().setSambaMungedDial(getString(source, "sambaMungedDial", null));
        dest.getSambaSettings().setSambaPrimaryGroupSID(getString(source,
                "sambaPrimaryGroupSID", null));
        dest.getSambaSettings().setSambaSID(getString(source, "sambaSID", null));

        // Mail
        String mailServer = getString(source, "gosaMailServer", null);
        if (StringUtils.isNotBlank(mailServer)) {
            if (dest.getMailSettings() == null) {
                dest.setMailSettings(new MailSettingsDto());
            }
            dest.getMailSettings().setGosaMailAlternateAddresses(getStringList(source,
                    "gosaMailAlternateAddress"));
            dest.getMailSettings().setGosaMailDeliveryMode(getString(source,
                    "gosaMailDeliveryMode", null));
            dest.getMailSettings().setGosaMailForwardingAddresses(getStringList(source,
                    "gosaMailForwardingAddress"));
            dest.getMailSettings().setGosaMailQuota(getString(source, "gosaMailQuota", null));
            dest.getMailSettings().setGosaMailServer(mailServer);
            dest.getMailSettings().setGosaSpamMailbox(getString(source, "gosaSpamMailbox", null));
            dest.getMailSettings().setGosaSpamSortLevel(getString(source, "gosaSpamSortLevel", null));
        }
    }

}
