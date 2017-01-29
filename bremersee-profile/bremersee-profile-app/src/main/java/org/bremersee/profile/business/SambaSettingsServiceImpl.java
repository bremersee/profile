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
import org.bremersee.common.exception.BadRequestException;
import org.bremersee.common.exception.ForbiddenException;
import org.bremersee.profile.domain.ldap.dao.UserProfileLdapDao;
import org.bremersee.profile.domain.ldap.entity.UserProfileLdap;
import org.bremersee.profile.model.SambaSettingsDto;
import org.bremersee.profile.model.UserGroupDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * @author Christian Bremer
 */
@Service("sambaSettingsService")
public class SambaSettingsServiceImpl extends AbstractUserProfileSettingsServiceImpl implements SambaSettingsService {

    @Autowired
    public SambaSettingsServiceImpl(
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
            + " or hasPermission(#userName, 'UserProfile#sambaSettings', 'create')")
    @Override
    public void applySambaSettingsToProfile(final String userName, final SambaSettingsDto sambaSettings) {
        setSambaSettingsToProfile(userName, sambaSettings);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SYSTEM')"
            + " or hasPermission(#userName, 'UserProfile#sambaSettings', 'write')")
    @Override
    public void updateSambaSettingsToProfile(final String userName, final SambaSettingsDto sambaSettings) {
        UserProfileLdap ldap = getUserProfile(userName);
        if (!ldap.hasSambaSettings() && !hasCreatePermission(userName)) {
            ForbiddenException fe = new ForbiddenException(
                    "You don't have the permission to create samba settings.");
            log.error("Updating samba settings failed.", fe);
            throw fe;
        }
        setSambaSettingsToProfile(userName, sambaSettings);
    }

    private boolean hasCreatePermission(final String userName) {
        return userName != null && (isCurrentUserAdminOrSystem() || getPermissionEvaluator().hasPermission(
                SecurityContextHolder.getContext().getAuthentication(),
                userName, "UserProfile#sambaSettings", BasePermission.CREATE));
    }

    private void setSambaSettingsToProfile(final String userName, final SambaSettingsDto sambaSettings) {

        log.info("{}: Applying samba settings [{}] to user [{}] ...", sambaSettings, userName);

        BadRequestException.validateNotNull(sambaSettings, "Samba settings must be present.");

        if (!doPosixSettingsExistsByUid(userName)) {
            applyDefaultPosixSettingsToProfile(userName);
        }

        UserProfileLdap ldap = getUserProfile(userName);

        // validate samba domain name
        if (StringUtils.isBlank(sambaSettings.getSambaDomainName())
                && ldap.getSambaSettings() != null
                && StringUtils.isNotBlank(ldap.getSambaSettings().getSambaDomainName())) {
            sambaSettings.setSambaDomainName(ldap.getSambaSettings().getSambaDomainName());
        } else if (StringUtils.isBlank(sambaSettings.getSambaDomainName())) {
            sambaSettings.setSambaDomainName(sambaDomainService.getDefaultSambaDomainName());
        } else {
            BadRequestException.validateTrue(
                    sambaDomainService.existsBySambaDomainName(sambaSettings.getSambaDomainName()),
                    String.format("Samba domain [%s] does not exist.", sambaSettings.getSambaDomainName()));
        }

        // validate Samba SID
        if (StringUtils.isBlank(sambaSettings.getSambaSID())
                && ldap.getSambaSettings() != null
                && StringUtils.isNotBlank(ldap.getSambaSettings().getSambaSID())) {
            sambaSettings.setSambaSID(ldap.getSambaSettings().getSambaSID());
        } else if (StringUtils.isBlank(sambaSettings.getSambaSID())) {
            long uidNumber = ldap.getPosixSettings().getUidNumber();
            String sambaDomainName = sambaSettings.getSambaDomainName();
            sambaSettings.setSambaSID(sambaDomainService.getSambaSID(uidNumber, sambaDomainName));
        } else {
            final int index = sambaSettings.getSambaSID().lastIndexOf('-');
            BadRequestException.validateTrue(index > 0,
                    String.format("Illegal Samba SID [%s]", sambaSettings.getSambaSID()));
            final String sid = sambaSettings.getSambaSID().substring(0, index);
            BadRequestException.validateTrue(sambaDomainService.existsBySambaSID(sid),
                    String.format("Samba SID [%s] doesn't exist.", sambaSettings.getSambaSID()));
        }

        // set Samba SID of primary group
        // (the primary group exists always since we have applied posix settings before)
        UserGroupDto primaryGroup = runAsSystem(() -> userGroupService
                .findByGidNumber(ldap.getPosixSettings().getGidNumber()));
        sambaSettings.setSambaPrimaryGroupSID(primaryGroup.getSambaSID());

        // validate 'sambaAcctFlags'
        if (StringUtils.isBlank(sambaSettings.getSambaAcctFlags())
                && ldap.getSambaSettings() != null
                && StringUtils.isNotBlank(ldap.getSambaSettings().getSambaAcctFlags())) {
            sambaSettings.setSambaAcctFlags(ldap.getSambaSettings().getSambaAcctFlags());
        } else if (StringUtils.isBlank(sambaSettings.getSambaAcctFlags())) {
            sambaSettings.setSambaAcctFlags("[U           ]");
        } // else validate value (once I've written a tool for this ...)

        // validate bad password counter
        if (sambaSettings.getSambaBadPasswordCount() == null
                && ldap.getSambaSettings() != null
                && ldap.getSambaSettings().getSambaBadPasswordCount() != null) {
            sambaSettings.setSambaBadPasswordCount(ldap.getSambaSettings().getSambaBadPasswordCount());
        } else if (sambaSettings.getSambaBadPasswordCount() == null) {
            sambaSettings.setSambaBadPasswordCount(0);
        }

        ldap.setSambaSettings(sambaSettings);

        userProfileLdapDao.save(ldap);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SYSTEM')"
            + " or hasPermission(#userName, 'UserProfile#sambaSettings', 'read')")
    @Override
    public SambaSettingsDto findSambaSettingsByUid(final String userName) {
        log.info("{}: Getting samba settings of user [{}] ...", getCurrentUserName(), userName);
        UserProfileLdap ldap = getUserProfile(userName);
        SambaSettingsDto settings = ldap.getSambaSettings() == null ? new SambaSettingsDto() : ldap.getSambaSettings();
        log.info("{}: Getting samba settings of user [{}]: {}", getCurrentUserName(), userName, settings);
        return settings;
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SYSTEM')"
            + " or hasPermission(#userName, 'UserProfile#sambaSettings', 'read')")
    @Override
    public boolean sambaSettingsExistsByUid(final String userName) {
        final boolean result = getUserProfile(userName).hasSambaSettings();
        log.info("{}: Has user profile [{}] samba settings? {}", getCurrentUserName(), userName, result);
        return result;
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SYSTEM')"
            + " or hasPermission(#userName, 'UserProfile#sambaSettings', 'delete')")
    @Override
    public void removeSambaSettingsFromProfile(final String userName) {
        log.info("{}: Removing samba settings from user profile [{}].", getCurrentUserName(), userName);
        UserProfileLdap ldap = getUserProfile(userName);
        ldap.setSambaSettings(null);
        userProfileLdapDao.save(ldap);
    }

}
