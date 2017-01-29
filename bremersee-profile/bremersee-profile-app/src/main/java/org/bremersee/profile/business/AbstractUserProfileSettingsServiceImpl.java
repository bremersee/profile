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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.bremersee.common.exception.BadRequestException;
import org.bremersee.common.exception.NotFoundException;
import org.bremersee.profile.domain.ldap.dao.UserProfileLdapDao;
import org.bremersee.profile.domain.ldap.entity.UserProfileLdap;
import org.bremersee.profile.model.PosixSettingsDto;
import org.bremersee.profile.model.UserGroupDto;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.Sid;

/**
 * @author Christian Bremer
 */
public abstract class AbstractUserProfileSettingsServiceImpl extends AbstractServiceImpl {

    private static final String USER_NAME_MUST_BE_PRESENT = "User name must be present.";

    final UserProfileLdapDao userProfileLdapDao;

    final UserGroupService userGroupService;

    final SambaDomainService sambaDomainService;

    public AbstractUserProfileSettingsServiceImpl(
            final UserProfileLdapDao userProfileLdapDao,
            final UserGroupService userGroupService,
            final SambaDomainService sambaDomainService) {

        this.userProfileLdapDao = userProfileLdapDao;
        this.userGroupService = userGroupService;
        this.sambaDomainService = sambaDomainService;

        Validate.notNull(this.userProfileLdapDao, "userProfileLdapDao must not be null");
        Validate.notNull(this.userGroupService, "userGroupService must not be null");
        Validate.notNull(this.sambaDomainService, "sambaDomainService must not be null");
    }

    private String getUserNotFoundMessage(final String userName) {
        return String.format("User with name [%s] was not found.", userName);
    }

    UserProfileLdap getUserProfile(final String userName) {
        BadRequestException.validateNotBlank(userName, USER_NAME_MUST_BE_PRESENT);
        UserProfileLdap ldap = userProfileLdapDao.findByUserName(userName);
        NotFoundException.validateNotNull(ldap, getUserNotFoundMessage(userName));
        return ldap;
    }

    boolean doPosixSettingsExistsByUid(final String userName) {
        return getUserProfile(userName).hasPosixSettings();
    }

    void applyDefaultPosixSettingsToProfile(final String userName) {
        doApplyPosixSettingsToProfile(userName, new PosixSettingsDto());
    }

    void doApplyPosixSettingsToProfile(final String userName,
                                       final PosixSettingsDto posixSettings) {

        BadRequestException.validateNotNull(posixSettings, "Posix settings must be present.");

        UserProfileLdap ldap = getUserProfile(userName);

        validateGidNumber(ldap, posixSettings);
        validateUidNumber(ldap, posixSettings);

        if (StringUtils.isBlank(posixSettings.getGecos())
                && ldap.getPosixSettings() != null
                && StringUtils.isNotBlank(ldap.getPosixSettings().getGecos())) {
            posixSettings.setGecos(ldap.getPosixSettings().getGecos());
        } else if (StringUtils.isBlank(posixSettings.getGecos())) {
            posixSettings.setGecos(ldap.getDisplayName());
        }

        if (StringUtils.isBlank(posixSettings.getHomeDirectory())
                && ldap.getPosixSettings() != null
                && StringUtils.isNotBlank(ldap.getPosixSettings().getHomeDirectory())) {
            posixSettings.setHomeDirectory(ldap.getPosixSettings().getHomeDirectory());
        } else if (StringUtils.isBlank(posixSettings.getHomeDirectory())) {
            posixSettings.setHomeDirectory("/home/" + userName);
        }

        if (StringUtils.isBlank(posixSettings.getLoginShell())
                && ldap.getPosixSettings() != null
                && StringUtils.isNotBlank(ldap.getPosixSettings().getLoginShell())) {
            posixSettings.setLoginShell(ldap.getPosixSettings().getLoginShell());
        } else if (StringUtils.isBlank(posixSettings.getLoginShell())) {
            posixSettings.setLoginShell("/bin/bash");
        }

        ldap.setPosixSettings(posixSettings);

        userProfileLdapDao.save(ldap);
    }

    private void validateGidNumber(final UserProfileLdap ldap, final PosixSettingsDto posixSettings) {
        if (posixSettings.getGidNumber() == null
                || (posixSettings.getGidNumber() != null
                && !userGroupService.existsByGidNumber(posixSettings.getGidNumber()))) {

            final Long gidNumber;
            if (userGroupService.existsByName(ldap.getUid())) {

                gidNumber = userGroupService.findByName(ldap.getUid()).getGidNumber();

            } else {

                UserGroupDto defaultUserGroup = new UserGroupDto();
                defaultUserGroup.setName(ldap.getUid());
                defaultUserGroup.setDescription("User group of '" + ldap.getUid() + "'.");

                defaultUserGroup.setGidNumber(posixSettings.getGidNumber());

                defaultUserGroup.setSambaGroup(false); // becomes a samba group at applySambaSettings
                defaultUserGroup.setSambaGroupType(null);
                defaultUserGroup.setSambaSID(null);

                defaultUserGroup = userGroupService.create(defaultUserGroup);

                gidNumber = defaultUserGroup.getGidNumber();

                MutableAcl groupAcl = (MutableAcl) getAclService().readAclById(
                        getObjectIdentityRetrievalStrategy().getObjectIdentity(defaultUserGroup));
                final Sid userSid = new PrincipalSid(ldap.getUid());
                groupAcl.insertAce(groupAcl.getEntries().size(), BasePermission.CREATE, userSid, true);
            }

            posixSettings.setGidNumber(gidNumber);
        }
    }

    private void validateUidNumber(final UserProfileLdap ldap, final PosixSettingsDto posixSettings) {
        if (posixSettings.getUidNumber() == null) {
            if (ldap.getPosixSettings() != null && ldap.getPosixSettings().getUidNumber() != null) {
                posixSettings.setUidNumber(ldap.getPosixSettings().getUidNumber());
            } else {
                final Long uid = sambaDomainService.getNextUidNumber();
                posixSettings.setUidNumber(uid);
            }
        } else {
            if (ldap.getPosixSettings() == null
                    || !posixSettings.getUidNumber().equals(ldap.getPosixSettings().getUidNumber())) {
                UserProfileLdap entity = userProfileLdapDao.findByUidNumber(posixSettings.getUidNumber());
                if (entity != null && !entity.getUid().equals(ldap.getUid())) {
                    BadRequestException bre = new BadRequestException(
                            String.format("UID number [%s] already exists.", posixSettings.getUidNumber()));
                    log.error("Applying posix settings failed.", bre);
                    throw bre;
                }
            }
        }
    }

}
