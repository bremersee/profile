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
import org.bremersee.profile.model.OrganisationSettingsDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * @author Christian Bremer
 */
@Service("organisationSettingsService")
public class OrganisationSettingsServiceImpl extends AbstractUserProfileSettingsServiceImpl
        implements OrganisationSettingsService {

    @Autowired
    public OrganisationSettingsServiceImpl(
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
            + " or hasPermission(#userName, 'UserProfile#organisationSettings', 'create')")
    @Override
    public void applyOrganisationSettingsToProfile(final String userName,
                                                   final OrganisationSettingsDto organisationSettings) {
        setOrganisationSettingsToProfile(userName, organisationSettings);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SYSTEM')"
            + " or hasPermission(#userName, 'UserProfile#organisationSettings', 'write')")
    @Override
    public void updateOrganisationSettingsToProfile(final String userName,
                                                    final OrganisationSettingsDto organisationSettings) {

        UserProfileLdap ldap = getUserProfile(userName);
        if (!ldap.hasOrganisationSettings() && !hasCreatePermission(userName)) {
            ForbiddenException fe = new ForbiddenException(
                    "You don't have the permission to create organisation settings.");
            log.error("Updating organisation settings failed.", fe);
            throw fe;
        }
        ldap.setOrganisationSettings(organisationSettings);
        userProfileLdapDao.save(ldap);
    }

    private boolean hasCreatePermission(final String userName) {
        return userName != null && (isCurrentUserAdminOrSystem() || getPermissionEvaluator().hasPermission(
                SecurityContextHolder.getContext().getAuthentication(),
                userName, "UserProfile#organisationSettings", BasePermission.CREATE));
    }

    private void setOrganisationSettingsToProfile(final String userName,
                                                  final OrganisationSettingsDto organisationSettings) {
        UserProfileLdap ldap = getUserProfile(userName);
        setOrganisationSettingsToProfile(ldap, organisationSettings);
    }

    private void setOrganisationSettingsToProfile(final UserProfileLdap ldap,
                                                  final OrganisationSettingsDto organisationSettings) {
        log.info("{}: Setting organisation settings [{}] to user [{}] ...", organisationSettings, ldap.getUid());
        BadRequestException.validateNotNull(organisationSettings, "Organisation settings must be present.");
        ldap.setOrganisationSettings(organisationSettings);
        userProfileLdapDao.save(ldap);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SYSTEM','ROLE_READ_ALL_PROFILES')"
            + " or hasPermission(#userName, 'UserProfile#organisationSettings', 'read')")
    @Override
    public OrganisationSettingsDto findOrganisationSettingsByUid(final String userName) {

        log.info("{}: Getting organisation settings of user [{}] ...", getCurrentUserName(), userName);
        UserProfileLdap ldap = getUserProfile(userName);
        OrganisationSettingsDto settings = ldap.getOrganisationSettings() == null ? new OrganisationSettingsDto() :
                ldap.getOrganisationSettings();
        log.info("{}: Getting organisation settings of user [{}]: {}", getCurrentUserName(), userName, settings);
        return settings;
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SYSTEM')"
            + " or hasPermission(#userName, 'UserProfile#organisationSettings', 'delete')")
    @Override
    public void removeOrganisationSettingsFromProfile(final String userName) {

        log.info("{}: Removing organisation settings from user profile [{}].", getCurrentUserName(), userName);
        UserProfileLdap ldap = getUserProfile(userName);
        ldap.setOrganisationSettings(null);
        userProfileLdapDao.save(ldap);
    }

}
