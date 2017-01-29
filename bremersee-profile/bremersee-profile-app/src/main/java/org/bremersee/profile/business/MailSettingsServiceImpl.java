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

import org.bremersee.common.exception.BadRequestException;
import org.bremersee.common.exception.ForbiddenException;
import org.bremersee.profile.domain.ldap.dao.UserProfileLdapDao;
import org.bremersee.profile.domain.ldap.entity.UserProfileLdap;
import org.bremersee.profile.model.MailSettingsDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * @author Christian Bremer
 */
@Service("mailSettingsService")
public class MailSettingsServiceImpl extends AbstractUserProfileSettingsServiceImpl implements MailSettingsService {

    @Autowired
    public MailSettingsServiceImpl(
            final UserProfileLdapDao userProfileLdapDao,
            final UserGroupService userGroupService,
            final SambaDomainService sambaDomainService) {
        super(userProfileLdapDao, userGroupService, sambaDomainService);
    }

    @Override
    protected void doInit() {
        // nothing to init
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SYSTEM')"
            + " or hasPermission(#userName, 'UserProfile#mailSettings', 'create')")
    @Override
    public void applyMailSettingsToProfile(final String userName, final MailSettingsDto mailSettings) {
        setMailSettingsToProfile(userName, mailSettings);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SYSTEM')"
            + " or hasPermission(#userName, 'UserProfile#mailSettings', 'write')")
    @Override
    public void updateMailSettingsToProfile(final String userName, final MailSettingsDto mailSettings) {
        UserProfileLdap ldap = getUserProfile(userName);
        if (!ldap.hasMailSettings() && !hasCreatePermission(userName)) {
            ForbiddenException fe = new ForbiddenException(
                    "You don't have the permission to create mail settings.");
            log.error("Updating mail settings failed.", fe);
            throw fe;
        }
        setMailSettingsToProfile(userName, mailSettings);
    }

    private boolean hasCreatePermission(final String userName) {
        return userName != null && (isCurrentUserAdminOrSystem() || getPermissionEvaluator().hasPermission(
                SecurityContextHolder.getContext().getAuthentication(),
                userName, "UserProfile#mailSettings", BasePermission.CREATE));
    }

    private void setMailSettingsToProfile(final String userName, final MailSettingsDto mailSettings) {

        log.info("{}: Settings mail settings [{}] to user [{}] ...", mailSettings, userName);
        BadRequestException.validateNotNull(mailSettings, "MailSettings must be present.");
        if (!doPosixSettingsExistsByUid(userName)) {
            applyDefaultPosixSettingsToProfile(userName);
        }
        UserProfileLdap ldap = getUserProfile(userName);
        ldap.setMailSettings(mailSettings);
        userProfileLdapDao.save(ldap);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SYSTEM')"
            + " or hasPermission(#userName, 'UserProfile#mailSettings', 'read')")
    @Override
    public MailSettingsDto findMailSettingsByUid(final String userName) {
        log.info("{}: Getting mail settings of user [{}] ...", getCurrentUserName(), userName);
        UserProfileLdap ldap = getUserProfile(userName);
        MailSettingsDto settings = ldap.getMailSettings() == null ? new MailSettingsDto() : ldap.getMailSettings();
        log.info("{}: Getting mail settings of user [{}]: {}", getCurrentUserName(), userName, settings);
        return settings;
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SYSTEM')"
            + " or hasPermission(#userName, 'UserProfile#mailSettings', 'read')")
    @Override
    public boolean mailSettingsExistsByUid(final String userName) {
        final boolean result = getUserProfile(userName).hasMailSettings();
        log.info("{}: Has user profile [{}] mail settings? {}", getCurrentUserName(), userName, result);
        return result;
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SYSTEM')"
            + " or hasPermission(#userName, 'UserProfile#mailSettings', 'delete')")
    @Override
    public void removeMailSettingsFromProfile(final String userName) {
        log.info("{}: Removing mail settings from user profile [{}].", getCurrentUserName(), userName);
        UserProfileLdap ldap = getUserProfile(userName);
        ldap.setMailSettings(null);
        userProfileLdapDao.save(ldap);
    }

}
