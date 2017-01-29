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
import org.bremersee.profile.domain.ldap.entity.SambaDomainLdap;
import org.ldaptive.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * @author Christian Bremer
 */
@Component("sambaDomainLdapDao")
@EnableConfigurationProperties(SambaDomainLdapProperties.class)
public class SambaDomainLdapDaoImpl extends AbstractLdapDaoImpl implements SambaDomainLdapDao {

    private final SambaDomainLdapEntryMapper ldapEntryMapper;

    private SambaDomainLdapProperties properties = new SambaDomainLdapProperties();

    @Autowired
    public SambaDomainLdapDaoImpl(SambaDomainLdapEntryMapper ldapEntryMapper) {
        this.ldapEntryMapper = ldapEntryMapper;
    }

    @Autowired(required = false)
    public void setProperties(SambaDomainLdapProperties properties) {
        this.properties = properties;
    }

    @Override
    void doInit() {
        log.info("properties = {}", ldapEntryMapper.getClass().getName());
        log.info("ldapEntryMapper = {}", ldapEntryMapper.getClass().getName());
    }

    @Override
    AbstractLdapProperties getProperties() {
        return properties;
    }


    private LdapEntry findLdapEntryBySambaDomainName(final String sambaDomainName) {
        final String filter = String.format("(&(objectClass=sambaDomain)(sambaDomainName=%s))", sambaDomainName);
        return findOneByFilter(filter);
    }

    private LdapEntry findLdapEntryBySambaDomainName(final Connection connection, final String sambaDomainName)
            throws LdapException {
        final String filter = String.format("(&(objectClass=sambaDomain)(sambaDomainName=%s))", sambaDomainName);
        return findOneByFilter(connection, filter);
    }

    private LdapEntry findLdapEntryBySambaSID(final String sambaSID) {
        final String filter = String.format("(&(objectClass=sambaDomain)(sambaSID=%s))", sambaSID);
        return findOneByFilter(filter);
    }

    private LdapEntry findLdapEntryBySambaDomainNameOrSambaSID(final String sambaDomainNameOrSambaSID) {
        final String filter = String.format("(&(objectClass=sambaDomain)(|(sambaDomainName=%s)(sambaSID=%s)))",
                sambaDomainNameOrSambaSID, sambaDomainNameOrSambaSID);
        return findOneByFilter(filter);
    }

    @Override
    public SambaDomainLdap save(final SambaDomainLdap ldapEntity) {

        Connection connection = null;
        try {
            connection = getConnection();
            LdapEntry target = findLdapEntryBySambaDomainName(connection, ldapEntity.getSambaDomainName());
            doSave(connection, ldapEntity, target, ldapEntryMapper);
            return ldapEntity;

        } catch (final LdapException e) {

            InternalServerError ise = new InternalServerError(e);
            if (log.isErrorEnabled()) {
                log.error(String.format("Saving %s failed.", ldapEntity), ise);
            }
            throw ise;

        } finally {
            closeConnection(connection);
        }
    }

    @Override
    public Page<SambaDomainLdap> findAll(final PageRequest request) {

        final PageRequest pageRequest = request == null ? new PageRequestDto() : request;
        final String filter = findAllFilter(pageRequest.getQuery());
        Collection<LdapEntry> allLdapEntries = findByFilter(filter);
        return pageBuilder.buildFilteredPage(allLdapEntries, pageRequest, null, ldapEntryMapper::toEntity);
    }

    private String findAllFilter(String query) {
        final String objectClassFilter = "(objectClass=sambaDomain)";
        final String filter;
        if (StringUtils.isBlank(query)) {
            filter = objectClassFilter;
        } else {
            filter = "(&" + objectClassFilter + "(|"
                    + "(sambaDomainName=*" + query + "*)"
                    + "(sambaSID=*" + query + "*)"
                    + "))";
        }
        if (log.isDebugEnabled()) {
            log.debug("Find all roles with page request [...]: Using LDAP filter [" + filter + "] ...");
        }
        return filter;
    }

    @Override
    public SambaDomainLdap findBySambaDomainName(final String sambaDomainName) {

        BadRequestException.validateNotBlank(sambaDomainName, "Samba domain name must be present.");
        LdapEntry entry = findLdapEntryBySambaDomainName(sambaDomainName);
        if (entry == null) {
            return null;
        }
        return ldapEntryMapper.toEntity(entry);
    }

    @Override
    public SambaDomainLdap findBySambaSID(final String sambaSID) {

        BadRequestException.validateNotBlank(sambaSID, "Samba SID must be present.");
        LdapEntry entry = findLdapEntryBySambaSID(sambaSID);
        if (entry == null) {
            return null;
        }
        return ldapEntryMapper.toEntity(entry);
    }

    @Override
    public SambaDomainLdap findBySambaDomainNameOrSambaSID(final String sambaDomainNameOrSambaSID) {

        BadRequestException.validateNotBlank(sambaDomainNameOrSambaSID,
                "Samba domain name or samba SID must be present.");
        LdapEntry entry = findLdapEntryBySambaDomainNameOrSambaSID(sambaDomainNameOrSambaSID);
        if (entry == null) {
            return null;
        }
        return ldapEntryMapper.toEntity(entry);
    }

    @Override
    public boolean existsBySambaDomainName(final String sambaDomainName) {

        BadRequestException.validateNotBlank(sambaDomainName, "Samba domain name must be present.");
        return findLdapEntryBySambaDomainName(sambaDomainName) != null;
    }

    @Override
    public boolean existsBySambaSID(final String sambaSID) {

        BadRequestException.validateNotBlank(sambaSID, "Samba SID must be present.");
        return findLdapEntryBySambaSID(sambaSID) != null;
    }

    @Override
    public boolean existsBySambaDomainNameOrSambaSID(final String sambaDomainNameOrSambaSID) {

        BadRequestException.validateNotBlank(sambaDomainNameOrSambaSID,
                "Samba domain name or samba SID must be present.");
        return findLdapEntryBySambaDomainNameOrSambaSID(sambaDomainNameOrSambaSID) != null;
    }

    @Override
    public void deleteBySambaDomainName(final String sambaDomainName) {

        BadRequestException.validateNotBlank(sambaDomainName, "Domain name must be present.");
        final String dn = ldapEntryMapper.createDn(sambaDomainName);
        Connection connection = null;
        try {
            connection = getConnection();
            final DeleteOperation delete = new DeleteOperation(connection);
            final DeleteRequest request = new DeleteRequest(dn);
            final Response<Void> res = delete.execute(request);
            boolean result = res.getResultCode() == ResultCode.SUCCESS;
            if (log.isDebugEnabled()) {
                log.debug("Samba domain [" + sambaDomainName + "] "
                        + (result ? "successfully removed." : "doesn't exist. So it can't be removed."));
            }

        } catch (final LdapException e) {

            InternalServerError ise = new InternalServerError(e);
            log.error("Deleting of samba domain [" + sambaDomainName + "] failed.", ise);
            throw ise;

        } finally {
            closeConnection(connection);
        }
    }

}
