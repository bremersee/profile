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

import org.apache.commons.lang3.StringUtils;
import org.bremersee.common.exception.BadRequestException;
import org.bremersee.common.exception.InternalServerError;
import org.bremersee.common.exception.NotFoundException;
import org.bremersee.pagebuilder.model.Page;
import org.bremersee.pagebuilder.model.PageRequest;
import org.bremersee.pagebuilder.model.PageRequestDto;
import org.bremersee.profile.domain.ldap.entity.UserGroupLdap;
import org.ldaptive.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Christian Bremer
 */
@Component("userGroupLdapDao")
@EnableConfigurationProperties(UserGroupLdapProperties.class)
public class UserGroupLdapDaoImpl extends AbstractLdapDaoImpl implements UserGroupLdapDao {

    private static final String GROUP_NAME_MUST_BE_PRESENT = "Group name must be present.";

    private final UserGroupLdapEntryMapper ldapEntryMapper;

    private UserGroupLdapProperties properties = new UserGroupLdapProperties();

    @Autowired
    public UserGroupLdapDaoImpl(final UserGroupLdapEntryMapper ldapEntryMapper) {
        this.ldapEntryMapper = ldapEntryMapper;
    }

    @Autowired(required = false)
    public void setProperties(UserGroupLdapProperties properties) {
        this.properties = properties;
    }

    @Override
    void doInit() {
        log.info("ldapEntryMapper = " + ldapEntryMapper.getClass().getName());
    }

    @Override
    AbstractLdapProperties getProperties() {
        return properties;
    }


    @Override
    public Page<UserGroupLdap> findAll(final PageRequest request) {

        final PageRequest pageRequest = request == null ? new PageRequestDto() : request;
        final String filter = findAllFilter(pageRequest.getQuery());
        final Collection<LdapEntry> allLdapEntries = findByFilter(filter);
        return pageBuilder.buildFilteredPage(allLdapEntries, pageRequest, null, ldapEntryMapper::toEntity);
    }

    private String findAllFilter(String query) {
        final String objectClassFilter = "(objectClass=groupOfNames)";
        final String filter;
        if (StringUtils.isBlank(query)) {
            filter = objectClassFilter;
        } else {
            filter = "(&" + objectClassFilter + "(|"
                    + "(cn=*" + query + "*)"
                    + "(description=*" + query + "*)"
                    + "roleOccupant=*" + query + "*)"
                    + "))";
        }
        if (log.isDebugEnabled()) {
            log.debug("Find all user groups with page request [...]: Using LDAP filter [" + filter + "] ...");
        }
        return filter;
    }

    @Override
    public UserGroupLdap save(final UserGroupLdap entity) {

        BadRequestException.validateNotNull(entity, "User group must not be null.");
        BadRequestException.validateNotBlank(entity.getName(), GROUP_NAME_MUST_BE_PRESENT);
        Connection connection = null;
        try {
            connection = getConnection();
            LdapEntry target = findLdapEntryByName(connection, entity.getName());
            doSave(connection, entity, target, ldapEntryMapper);
            return entity;

        } catch (final LdapException e) {

            InternalServerError ise = new InternalServerError(e);
            log.error("Saving user group [" + entity + "] failed.", ise);
            throw ise;

        } finally {
            closeConnection(connection);
        }
    }

    @Override
    public UserGroupLdap findByName(final String userGroupName) {

        LdapEntry ldapEntry = findLdapEntryByName(userGroupName);
        if (ldapEntry == null) {
            return null;
        }
        return ldapEntryMapper.toEntity(ldapEntry);
    }

    @Override
    public boolean existsByName(final String userGroupName) {

        BadRequestException.validateNotBlank(userGroupName, GROUP_NAME_MUST_BE_PRESENT);
        return findLdapEntryByName(userGroupName) != null;
    }

    @Override
    public UserGroupLdap findByGidNumber(final long gidNumber) {

        LdapEntry ldapEntry = findLdapEntryByGidNumber(gidNumber);
        if (ldapEntry == null) {
            return null;
        }
        return ldapEntryMapper.toEntity(ldapEntry);
    }

    @Override
    public boolean existsByGidNumber(final long gidNumber) {

        return findLdapEntryByGidNumber(gidNumber) != null;
    }

    @Override
    public UserGroupLdap findBySambaSID(final String sambaSID) {
        BadRequestException.validateNotBlank(sambaSID, "Samba SID must be present.");
        LdapEntry ldapEntry = findLdapEntryBySambaSID(sambaSID);
        if (ldapEntry == null) {
            return null;
        }
        return ldapEntryMapper.toEntity(ldapEntry);
    }

    @Override
    public boolean existsBySambaSID(final String sambaSID) {
        BadRequestException.validateNotBlank(sambaSID, "Samba SID must be present.");
        return findLdapEntryBySambaSID(sambaSID) != null;
    }

    @Override
    public void deleteByName(final String userGroupName) {

        BadRequestException.validateNotBlank(userGroupName, GROUP_NAME_MUST_BE_PRESENT);
        final String dn = ldapEntryMapper.createDn(userGroupName);
        Connection connection = null;
        try {
            connection = getConnection();
            final DeleteOperation delete = new DeleteOperation(connection);
            final DeleteRequest request = new DeleteRequest(dn);
            final Response<Void> res = delete.execute(request);
            boolean result = res.getResultCode() == ResultCode.SUCCESS;
            if (log.isDebugEnabled()) {
                log.debug("User group [" + userGroupName + "] "
                        + (result ? "successfully removed." : "doesn't exist. So it can't be removed."));
            }

        } catch (final LdapException e) {

            InternalServerError ise = new InternalServerError(e);
            log.error("Deleting of user group [" + userGroupName + "] failed.", ise);
            throw ise;

        } finally {
            closeConnection(connection);
        }
    }

    @Override
    public Set<String> findUserGroupNamesByMember(final String member) {

        BadRequestException.validateNotBlank(member, "Member must be present.");
        String filter = "(&(objectClass=posixGroup)(objectClass=groupOfNames)(memberUid=" + member + "))";
        Set<String> userGroups = new LinkedHashSet<>();
        Collection<LdapEntry> allLdapEntries = findByFilter(filter, "cn");
        for (LdapEntry ldapEntry : allLdapEntries) {
            LdapAttribute attr = ldapEntry.getAttribute("cn");
            if (attr != null && StringUtils.isNotBlank(attr.getStringValue())) {
                userGroups.add(attr.getStringValue());
            }
        }
        return userGroups;
    }

    @Override
    public boolean hasUserGroup(final String member, final String userGroupName) {

        BadRequestException.validateNotBlank(member, "Member must be present.");
        BadRequestException.validateNotBlank(userGroupName, GROUP_NAME_MUST_BE_PRESENT);
        return findUserGroupNamesByMember(member).contains(userGroupName);
    }

    @Override
    public Set<String> getMembers(final String userGroupName) {

        BadRequestException.validateNotBlank(userGroupName, GROUP_NAME_MUST_BE_PRESENT);
        LdapEntry ldapEntry = findLdapEntryByName(userGroupName);
        UserGroupLdap userGroup = new UserGroupLdap();
        ldapEntryMapper.map(ldapEntry, userGroup);
        return userGroup.getMembers();
    }

    @Override
    public void addMembers(final String userGroupName, final Collection<String> members) {

        if (members != null && !members.isEmpty()) {
            BadRequestException.validateNotBlank(userGroupName, GROUP_NAME_MUST_BE_PRESENT);
            LdapEntry ldapEntry = findLdapEntryByName(userGroupName);
            NotFoundException.validateNotNull(ldapEntry, "Group with name [" // NOSONAR
                    + userGroupName + "] was not found."); // NOSONAR
            UserGroupLdap userGroup = new UserGroupLdap();
            ldapEntryMapper.map(ldapEntry, userGroup);
            userGroup.getMembers().addAll(members);
            save(userGroup);
        }
    }

    @Override
    public void removeMembers(final String userGroupName, final Collection<String> members) {

        if (members != null && !members.isEmpty()) {
            BadRequestException.validateNotBlank(userGroupName, GROUP_NAME_MUST_BE_PRESENT);
            LdapEntry ldapEntry = findLdapEntryByName(userGroupName);
            NotFoundException.validateNotNull(ldapEntry, "Group with name [" + userGroupName
                    + "] was not found.");
            UserGroupLdap userGroup = new UserGroupLdap();
            ldapEntryMapper.map(ldapEntry, userGroup);
            userGroup.getMembers().removeAll(members);
            save(userGroup);
        }
    }

    @Override
    public void updateMembers(final String userGroupName, final Collection<String> members) {

        BadRequestException.validateNotBlank(userGroupName, GROUP_NAME_MUST_BE_PRESENT);
        LdapEntry ldapEntry = findLdapEntryByName(userGroupName);
        NotFoundException.validateNotNull(ldapEntry, "Group with name [" + userGroupName + "] was not found.");
        UserGroupLdap userGroup = new UserGroupLdap();
        ldapEntryMapper.map(ldapEntry, userGroup);
        userGroup.getMembers().clear();
        if (members != null) {
            userGroup.getMembers().addAll(members);
        }
        save(userGroup);
    }

    private LdapEntry findLdapEntryByName(final String name) {
        final String filter = "(&(objectClass=posixGroup)(objectClass=groupOfNames)(cn=" + name + "))";
        return findOneByFilter(filter);
    }

    private LdapEntry findLdapEntryByName(final Connection connection, final String name) throws LdapException {
        final String filter = "(&(objectClass=posixGroup)(objectClass=groupOfNames)(cn=" + name + "))";
        return findOneByFilter(connection, filter);
    }

    private LdapEntry findLdapEntryByGidNumber(final long gidNumber) {
        final String filter = "(&(objectClass=posixGroup)(objectClass=groupOfNames)(gidNumber=" + gidNumber + "))";
        return findOneByFilter(filter);
    }

    private LdapEntry findLdapEntryBySambaSID(String sambaSID) {
        final String filter = "(&(objectClass=posixGroup)(objectClass=groupOfNames)(sambaSID=" + sambaSID + "))";
        return findOneByFilter(filter);
    }

}
