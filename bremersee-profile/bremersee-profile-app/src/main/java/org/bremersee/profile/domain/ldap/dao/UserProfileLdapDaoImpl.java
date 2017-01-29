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
import org.bremersee.common.exception.BadRequestException;
import org.bremersee.common.exception.InternalServerError;
import org.bremersee.pagebuilder.model.Page;
import org.bremersee.pagebuilder.model.PageRequest;
import org.bremersee.pagebuilder.model.PageRequestDto;
import org.bremersee.profile.domain.ldap.entity.UserProfileLdap;
import org.ldaptive.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * @author Christian Bremer
 */
@Component("userProfileLdapDao")
@EnableConfigurationProperties(UserProfileLdapProperties.class)
public class UserProfileLdapDaoImpl extends AbstractLdapDaoImpl implements UserProfileLdapDao {

    private UserProfileLdapProperties properties;

    private final UserProfileLdapEntryMapper ldapEntryMapper;

    @Autowired
    public UserProfileLdapDaoImpl(final UserProfileLdapEntryMapper ldapEntryMapper) {
        this.ldapEntryMapper = ldapEntryMapper;
    }

    @Autowired(required = false)
    public void setProperties(UserProfileLdapProperties properties) {
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
    public Page<UserProfileLdap> findAll(final PageRequest request) {

        final PageRequest pageRequest = request == null ? new PageRequestDto() : request;
        String filter = findAllFilter(pageRequest.getQuery());
        Collection<LdapEntry> allLdapEntries = findByFilter(filter);
        return pageBuilder.buildFilteredPage(allLdapEntries, pageRequest, null, ldapEntryMapper::toEntity);
    }

    private String findAllFilter(final String query) {
        final String objectClassFilter = "(&(objectClass=inetOrgPerson)(objectClass=gosaAccount))";
        final String filter;
        if (StringUtils.isBlank(query)) {
            filter = objectClassFilter;
        } else {
            filter = "(&" + objectClassFilter + "(|"
                    + "(uid=*" + query + "*)"
                    + "(mail=*" + query + "*)"
                    + "givenName=*" + query + "*)"
                    + "sn=*" + query + "*)"
                    + "cn=*" + query + "*)"
                    + "))";
        }
        if (log.isDebugEnabled()) {
            log.debug("Find all user profiles with page request [...]: Using LDAP filter [" + filter + "] ...");
        }
        return filter;
    }

    @Override
    public UserProfileLdap save(final UserProfileLdap entity) {

        BadRequestException.validateNotNull(entity, "User profile must not be null.");
        BadRequestException.validateNotBlank(entity.getUid(), "User name must be present."); // NOSONAR

        Connection connection = null;
        try {
            connection = getConnection();
            LdapEntry target = findLdapEntryByUserName(connection, entity.getUid());
            doSave(connection, entity, target, ldapEntryMapper);
            return entity;

        } catch (final LdapException e) {

            InternalServerError ise = new InternalServerError(e);
            log.error("Saving user profile [" + entity + "] failed.", ise);
            throw ise;

        } finally {
            closeConnection(connection);
        }
    }

    @Override
    public UserProfileLdap findByUserName(final String userName) {

        BadRequestException.validateNotBlank(userName, "User name must be present.");
        LdapEntry source = findLdapEntryByUserName(userName);
        if (source == null) {
            return null;
        }
        return ldapEntryMapper.toEntity(source);
    }

    private LdapEntry findLdapEntryByUserName(final String userName) {
        return findOneByFilter("(uid=" + userName + ")");
    }

    private LdapEntry findLdapEntryByUserName(final Connection connection, final String userName) throws LdapException {
        return findOneByFilter(connection, "(uid=" + userName + ")");
    }

    @Override
    public UserProfileLdap findByUidNumber(final long uidNumber) {

        LdapEntry source = findLdapEntryByUidNumber(uidNumber);
        if (source == null) {
            return null;
        }
        return ldapEntryMapper.toEntity(source);
    }

    private LdapEntry findLdapEntryByUidNumber(final long uidNumber) {
        return findOneByFilter("(uidNumber=" + uidNumber + ")");
    }

    @Override
    public UserProfileLdap findByEmail(final String email) {

        BadRequestException.validateNotBlank(email, "Email must be present.");
        LdapEntry source = findLdapEntryByEmail(email);
        if (source == null) {
            return null;
        }
        return ldapEntryMapper.toEntity(source);
    }

    private LdapEntry findLdapEntryByEmail(final String email) {
        return findOneByFilter("(mail=" + email + ")");
    }

    @Override
    public UserProfileLdap findByMobile(final String mobile) {

        BadRequestException.validateNotBlank(mobile, "Mobile number must be present.");
        LdapEntry source = findLdapEntryByMobile(mobile);
        if (source == null) {
            return null;
        }
        return ldapEntryMapper.toEntity(source);
    }

    private LdapEntry findLdapEntryByMobile(final String mobile) {
        return findOneByFilter("(mobile=" + mobile + ")");
    }

    @Override
    public boolean existsByUserName(final String userName) {
        return findLdapEntryByUserName(userName) != null;
    }

    @Override
    public boolean existsByUidNumber(final long uidNumber) {
        return findLdapEntryByUidNumber(uidNumber) != null;
    }

    @Override
    public boolean existsByEmail(final String email) {
        return findLdapEntryByEmail(email) != null;
    }

    @Override
    public boolean existsByMobile(final String mobile) {
        return findLdapEntryByMobile(mobile) != null;
    }

    @Override
    public void deleteByUserName(final String userName) {

        BadRequestException.validateNotBlank(userName, "User name must be present.");
        final String dn = ldapEntryMapper.createDn(userName);
        Connection connection = null;
        try {
            connection = getConnection();
            final DeleteOperation delete = new DeleteOperation(connection);
            final DeleteRequest request = new DeleteRequest(dn);
            final Response<Void> res = delete.execute(request);
            boolean result = res.getResultCode() == ResultCode.SUCCESS;
            if (log.isDebugEnabled()) {
                log.debug("User profile [" + userName + "] "
                        + (result ? "successfully deleted." : "doesn't exist. So it can't be deleted."));
            }

        } catch (final LdapException e) {

            InternalServerError ise = new InternalServerError(e);
            log.error("Deleting user profile [" + userName + "] failed.", ise);
            throw ise;

        } finally {
            closeConnection(connection);
        }
    }

}
