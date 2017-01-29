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

import org.bremersee.common.exception.ForbiddenException;
import org.bremersee.common.security.core.context.RunAsCallbackWithoutResult;
import org.bremersee.profile.domain.ldap.dao.UserProfileLdapDao;
import org.bremersee.profile.domain.ldap.entity.UserProfileLdap;
import org.bremersee.profile.model.PosixSettingsDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * @author Christian Bremer
 */
@Service("posixSettingsService")
public class PosixSettingsServiceImpl extends AbstractUserProfileSettingsServiceImpl implements PosixSettingsService {

    @Autowired
    public PosixSettingsServiceImpl(
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
            + " or hasPermission(#userName, 'UserProfile#posixSettings', 'create')")
    @Override
    public void applyPosixSettingsToProfile(final String userName,
                                            final PosixSettingsDto posixSettings) {

        log.info("{}: Applying posix settings [{}] to user [{}] ...", posixSettings, userName);
        doApplyPosixSettingsToProfile(userName, posixSettings);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SYSTEM')"
            + " or hasPermission(#userName, 'UserProfile#posixSettings', 'write')")
    @Override
    public void updatePosixSettingsToProfile(final String userName,
                                             final PosixSettingsDto posixSettings) {

        log.info("{}: Updating posix settings [{}] to user [{}] ...", posixSettings, userName);
        UserProfileLdap ldap = getUserProfile(userName);
        if (!ldap.hasPosixSettings() && !hasCreatePermission(userName)) {
            ForbiddenException fe = new ForbiddenException(
                    "You don't have the permission to create posix settings.");
            log.error("Updating posix settings failed.", fe);
            throw fe;
        }
        doApplyPosixSettingsToProfile(userName, posixSettings);
    }

    private boolean hasCreatePermission(final String userName) {
        return userName != null && (isCurrentUserAdminOrSystem() || getPermissionEvaluator().hasPermission(
                SecurityContextHolder.getContext().getAuthentication(),
                userName, "UserProfile#posixSettings", BasePermission.CREATE));
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SYSTEM')"
            + " or hasPermission(#userName, 'UserProfile#posixSettings', 'read')")
    @Override
    public PosixSettingsDto findPosixSettingsByUid(final String userName) {

        log.info("{}: Getting posix settings of user [{}] ...", getCurrentUserName(), userName);
        UserProfileLdap ldap = getUserProfile(userName);
        PosixSettingsDto settings = ldap.getPosixSettings() == null ? new PosixSettingsDto() : ldap.getPosixSettings();
        log.info("{}: Getting posix settings of user [{}]: {}", getCurrentUserName(), userName, settings);
        return settings;
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SYSTEM')"
            + " or hasPermission(#userName, 'UserProfile#posixSettings', 'read')")
    @Override
    public boolean posixSettingsExistsByUid(final String userName) {
        final boolean result = doPosixSettingsExistsByUid(userName);
        log.info("{}: Has user profile [{}] posix settings? {}", getCurrentUserName(), userName, result);
        return result;
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SYSTEM')"
            + " or hasPermission(#userName, 'UserProfile#posixSettings', 'delete')")
    @Override
    public void removePosixSettingsFromProfile(final String userName) {

        log.info("{}: Removing posix settings from user profile [{}].", getCurrentUserName(), userName);
        UserProfileLdap ldap = getUserProfile(userName);
        ldap.setPosixSettings(null);
        userProfileLdapDao.save(ldap);
        runAsSystem(new RunAsCallbackWithoutResult() {
            @Override
            public void run() {
                userGroupService.deleteByName(userName);
            }
        });
    }

}
