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
import org.bremersee.common.exception.BadRequestException;
import org.bremersee.common.exception.InternalServerError;
import org.bremersee.common.exception.NotFoundException;
import org.bremersee.pagebuilder.model.Page;
import org.bremersee.pagebuilder.model.PageRequest;
import org.bremersee.pagebuilder.model.PageRequestDto;
import org.bremersee.profile.domain.ldap.LdapEntryUtils;
import org.bremersee.profile.domain.ldap.entity.RoleLdap;
import org.ldaptive.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Christian Bremer
 */
@Component("roleLdapDao")
@EnableConfigurationProperties(RoleLdapProperties.class)
public class RoleLdapDaoImpl extends AbstractLdapDaoImpl implements RoleLdapDao {

    private static final String ROLE_NAME_MUST_BE_PRESENT = "Role name must be present.";

    private final RoleLdapEntryMapper ldapEntryMapper;

    private RoleLdapProperties properties = new RoleLdapProperties();

    @Autowired
    public RoleLdapDaoImpl(RoleLdapEntryMapper ldapEntryMapper) {
        this.ldapEntryMapper = ldapEntryMapper;
    }

    @Autowired(required = false)
    public void setProperties(RoleLdapProperties properties) {
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
    public Page<RoleLdap> findAll(final PageRequest request) {

        final PageRequest pageRequest = request == null ? new PageRequestDto() : request;
        final String filter = findAllFilter(pageRequest.getQuery());
        final Collection<LdapEntry> allLdapEntries = findByFilter(filter);
        return pageBuilder.buildFilteredPage(allLdapEntries, pageRequest, null, ldapEntryMapper::toEntity);
    }

    private String findAllFilter(String query) {
        final String objectClassFilter = "(objectClass=organizationalRole)";
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
            log.debug("Find all roles with page request [...]: Using LDAP filter [{}] ...", filter);
        }
        return filter;
    }

    @Override
    public RoleLdap save(final RoleLdap entity) {

        BadRequestException.validateNotNull(entity, "Role entity must be present.");
        BadRequestException.validateNotBlank(entity.getName(), ROLE_NAME_MUST_BE_PRESENT);

        Connection connection = null;
        try {
            connection = getConnection();
            LdapEntry target = findLdapEntryByName(connection, entity.getName());
            doSave(connection, entity, target, ldapEntryMapper);
            return entity;

        } catch (final LdapException e) {

            InternalServerError ise = new InternalServerError(e);
            log.error(String.format("Saving role [%s] failed.", entity), ise);
            throw ise;

        } finally {
            closeConnection(connection);
        }
    }

    @Override
    public RoleLdap findByName(final String roleName) {

        BadRequestException.validateNotBlank(roleName, ROLE_NAME_MUST_BE_PRESENT);
        LdapEntry ldapEntry = findLdapEntryByName(roleName);
        if (ldapEntry == null) {
            return null;
        }
        RoleLdap role = new RoleLdap();
        ldapEntryMapper.map(ldapEntry, role);
        return role;
    }

    @Override
    public Page<RoleLdap> findByNameStartsWith(final String roleNamePrefix, final PageRequest request) {

        BadRequestException.validateNotBlank(roleNamePrefix, "Role name prefix must be present."); // NOSONAR
        final PageRequest pageRequest = request == null ? new PageRequestDto() : request;
        final String filter = "(&(objectClass=organizationalRole)(cn=" + roleNamePrefix + "*))"; // NOSONAR
        final Collection<LdapEntry> allLdapEntries = findByFilter(filter);
        return pageBuilder.buildFilteredPage(allLdapEntries, pageRequest, null, ldapEntryMapper::toEntity);
    }

    @Override
    public boolean existsByName(final String roleName) {

        BadRequestException.validateNotBlank(roleName, ROLE_NAME_MUST_BE_PRESENT);
        return findLdapEntryByName(roleName) != null;
    }

    @Override
    public void deleteByName(final String roleName) {

        BadRequestException.validateNotBlank(roleName, ROLE_NAME_MUST_BE_PRESENT);
        final String dn = ldapEntryMapper.createDn(roleName);
        Connection connection = null;
        try {
            connection = getConnection();
            final DeleteOperation delete = new DeleteOperation(connection);
            final DeleteRequest request = new DeleteRequest(dn);
            final Response<Void> res = delete.execute(request);
            boolean result = res.getResultCode() == ResultCode.SUCCESS;
            if (log.isDebugEnabled()) {
                log.debug("Role [" + roleName + "] "
                        + (result ? "successfully removed." : "doesn't exist. So it can't be removed."));
            }

        } catch (final LdapException e) {

            InternalServerError ise = new InternalServerError(e);
            log.error("Deleting role [" + roleName + "] failed.", ise);
            throw ise;

        } finally {
            closeConnection(connection);
        }
    }

    @Override
    public Set<String> deleteByNameStartsWith(final String roleNamePrefix) {

        BadRequestException.validateNotBlank(roleNamePrefix, "Role name prefix must be present.");
        final Set<String> deletedRoleNames = new HashSet<>();
        final String filter = "(&(objectClass=organizationalRole)(cn=" + roleNamePrefix + "*))";
        Connection connection = null;
        try {
            connection = getConnection();
            Response<SearchResult> response = executeSearchOperation(connection, new SearchFilter(filter), "cn");
            if (hasResults(response)) {
                for (LdapEntry entry : response.getResult().getEntries()) {
                    final String roleName = LdapEntryUtils.getString(entry, "cn", null);
                    Validate.notNull(roleName, String.format("Role name is not available at LDAP entry with DN [%s].",
                            entry.getDn()));
                    log.debug("Deleting role with name [{}] ...", roleName);
                    final DeleteOperation delete = new DeleteOperation(connection);
                    final DeleteRequest request = new DeleteRequest(entry.getDn());
                    final Response<Void> res = delete.execute(request);
                    boolean result = res.getResultCode() == ResultCode.SUCCESS;
                    if (result) { // NOSONAR
                        deletedRoleNames.add(roleName);
                    }
                    log.debug("Role [{}] {}", roleName,
                            result ? "successfully deleted." : "doesn't exist. So it can't be deleted.");
                }
            }

        } catch (final LdapException e) {

            InternalServerError ise = new InternalServerError(e);
            log.error(String.format("Deleting role [%s] failed.", roleNamePrefix), ise);
            throw ise;

        } finally {
            closeConnection(connection);
        }
        return deletedRoleNames;
    }

    @Override
    public Set<String> findRoleNamesByMember(final String member) {

        BadRequestException.validateNotBlank(member, "Member must be present."); // NOSONAR
        String filter = "(&(objectClass=organizationalRole)(roleOccupant=" + ldapEntryMapper.createMemberDn(member) + "))";
        Set<String> roles = new LinkedHashSet<>();
        Collection<LdapEntry> allLdapEntries = findByFilter(filter, "cn");
        for (LdapEntry ldapEntry : allLdapEntries) {
            LdapAttribute attr = ldapEntry.getAttribute("cn");
            if (attr != null && StringUtils.isNotBlank(attr.getStringValue())) {
                roles.add(attr.getStringValue());
            }
        }
        return roles;
    }

    @Override
    public Set<String> findRoleNamesByMemberAndRoleNamePrefix(final String member, final String roleNamePrefix) {

        BadRequestException.validateNotBlank(member, "Member must be present.");
        BadRequestException.validateNotBlank(roleNamePrefix, "Role name prefix must be present.");
        String filter = "(&"
                + "(objectClass=organizationalRole)"
                + "(cn=" + roleNamePrefix + "*)"
                + "(roleOccupant=" + ldapEntryMapper.createMemberDn(member) + ")"
                + ")";
        Set<String> roles = new LinkedHashSet<>();
        Collection<LdapEntry> allLdapEntries = findByFilter(filter, "cn");
        for (LdapEntry ldapEntry : allLdapEntries) {
            LdapAttribute attr = ldapEntry.getAttribute("cn");
            if (attr != null && StringUtils.isNotBlank(attr.getStringValue())) {
                roles.add(attr.getStringValue());
            }
        }
        return roles;
    }

    @Override
    public boolean hasRole(final String member, final String roleName) {

        BadRequestException.validateNotBlank(member, "Member must be present.");
        BadRequestException.validateNotBlank(roleName, ROLE_NAME_MUST_BE_PRESENT);
        return findRoleNamesByMember(member).contains(roleName);
    }

    @Override
    public Set<String> getMembers(final String roleName) {

        BadRequestException.validateNotBlank(roleName, ROLE_NAME_MUST_BE_PRESENT);
        LdapEntry ldapEntry = findLdapEntryByName(roleName);
        NotFoundException.validateNotNull(ldapEntry, "Role with name [" + roleName // NOSONAR
                + "] was not found."); // NOSONAR
        RoleLdap role = new RoleLdap();
        ldapEntryMapper.map(ldapEntry, role);
        return role.getMembers();
    }

    @Override
    public void addMembers(final String roleName, final Collection<String> members) {

        BadRequestException.validateNotBlank(roleName, ROLE_NAME_MUST_BE_PRESENT);
        if (members != null && !members.isEmpty()) {
            LdapEntry ldapEntry = findLdapEntryByName(roleName);
            NotFoundException.validateNotNull(ldapEntry, "Role with name [" + roleName + "] was not found.");
            RoleLdap role = new RoleLdap();
            ldapEntryMapper.map(ldapEntry, role);
            role.getMembers().addAll(members);
            save(role);
        }
    }

    @Override
    public void removeMembers(final String roleName, final Collection<String> members) {

        BadRequestException.validateNotBlank(roleName, ROLE_NAME_MUST_BE_PRESENT);
        if (members != null && !members.isEmpty()) {
            LdapEntry ldapEntry = findLdapEntryByName(roleName);
            NotFoundException.validateNotNull(ldapEntry, "Role with name [" + roleName + "] was not found.");
            RoleLdap role = new RoleLdap();
            ldapEntryMapper.map(ldapEntry, role);
            role.getMembers().removeAll(members);
            save(role);
        }
    }

    @Override
    public void updateMembers(final String roleName, final Collection<String> members) {

        BadRequestException.validateNotBlank(roleName, ROLE_NAME_MUST_BE_PRESENT);
        LdapEntry ldapEntry = findLdapEntryByName(roleName);
        NotFoundException.validateNotNull(ldapEntry, "Role with name [" + roleName + "] was not found.");
        RoleLdap role = new RoleLdap();
        ldapEntryMapper.map(ldapEntry, role);
        role.getMembers().clear();
        if (members != null) {
            role.getMembers().addAll(members);
        }
        save(role);
    }

    private LdapEntry findLdapEntryByName(final String name) {
        Validate.notBlank(name, "Name must not be null or blank.");
        String filter = "(&(objectClass=organizationalRole)(cn=" + name + "))";
        return findOneByFilter(filter);
    }

    private LdapEntry findLdapEntryByName(final Connection connection, final String name) throws LdapException {
        Validate.notBlank(name, "Name must not be null or blank.");
        String filter = "(&(objectClass=organizationalRole)(cn=" + name + "))";
        return findOneByFilter(connection, filter);
    }

}
