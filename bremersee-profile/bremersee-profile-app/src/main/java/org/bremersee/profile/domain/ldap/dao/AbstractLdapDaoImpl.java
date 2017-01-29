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

import org.apache.commons.lang3.Validate;
import org.bremersee.common.exception.InternalServerError;
import org.bremersee.pagebuilder.PageBuilder;
import org.bremersee.pagebuilder.PageBuilderImpl;
import org.ldaptive.*;
import org.ldaptive.beans.LdapEntryMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Christian Bremer
 */
public abstract class AbstractLdapDaoImpl {

    final Logger log = LoggerFactory.getLogger(getClass());

    private ConnectionFactory connectionFactory;

    PageBuilder pageBuilder = new PageBuilderImpl();

    @Autowired
    public void setConnectionFactory(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Autowired(required = false)
    public void setPageBuilder(PageBuilder pageBuilder) {
        if (pageBuilder != null) {
            this.pageBuilder = pageBuilder;
        }
    }

    @PostConstruct
    public void init() {
        log.info("Initializing " + getClass().getSimpleName() + " ...");
        Validate.notNull(connectionFactory, "connectionFactory must not be null");
        Validate.notNull(getProperties(), "properties must not be null");
        Validate.notNull(getProperties().getSearchRequest(), "searchRequest must not be null");
        log.info("properties = {}", getProperties());
        doInit();
        log.info(getClass().getSimpleName() + " successfully initialized.");
    }

    abstract void doInit();

    abstract AbstractLdapProperties getProperties();

    /**
     * Gets connection from the factory. Opens the connection if needed.
     *
     * @return the connection
     * @throws LdapException the ldap exception
     */
    Connection getConnection() throws LdapException {
        final Connection c = this.connectionFactory.getConnection();
        if (!c.isOpen()) {
            c.open();
        }
        return c;
    }

    /**
     * Close the given context and ignore any thrown exception. This is useful
     * for typical finally blocks in manual Ldap statements.
     *
     * @param context the Ldap connection to close
     */
    void closeConnection(final Connection context) {
        if (context != null && context.isOpen()) {
            try {
                context.close();
            } catch (final Exception ex) {
                log.warn("Closing ldap connection failed.", ex);
            }
        }
    }

    /**
     * Builds a new request.
     *
     * @param filter the filter
     * @return the search request
     */
    private SearchRequest newRequest(final SearchFilter filter, final String... returnAttributes) {

        final SearchRequest searchRequestTemplate = getProperties().getSearchRequest();

        final SearchRequest sr;
        if (returnAttributes == null || returnAttributes.length == 0) {
            sr = new SearchRequest(searchRequestTemplate.getBaseDn(), filter);
        } else {
            sr = new SearchRequest(searchRequestTemplate.getBaseDn(), filter, returnAttributes);
        }

        sr.setBinaryAttributes(ReturnAttributes.ALL_USER.value());
        sr.setDerefAliases(searchRequestTemplate.getDerefAliases());
        sr.setSearchEntryHandlers(searchRequestTemplate.getSearchEntryHandlers());
        sr.setSearchReferenceHandlers(searchRequestTemplate.getSearchReferenceHandlers());
        // new in 1.2
        //sr.setFollowReferrals(searchRequestTemplate.getFollowReferrals()); // NOSONAR
        sr.setReturnAttributes(ReturnAttributes.ALL_USER.value());
        sr.setSearchScope(searchRequestTemplate.getSearchScope());
        sr.setSizeLimit(searchRequestTemplate.getSizeLimit());
        sr.setSortBehavior(searchRequestTemplate.getSortBehavior());
        // new in 1.2
        //sr.setTimeLimit(searchRequestTemplate.getTimeLimit()); // time limit is now of type duration
        sr.setTypesOnly(searchRequestTemplate.getTypesOnly());
        sr.setControls(searchRequestTemplate.getControls());
        // new in 1.2
        sr.setControls(searchRequestTemplate.getControls());
        sr.setIntermediateResponseHandlers(searchRequestTemplate.getIntermediateResponseHandlers());
        sr.setReferralHandler(searchRequestTemplate.getReferralHandler());
        sr.setTimeLimit(searchRequestTemplate.getTimeLimit());

        return sr;
    }

    /**
     * Execute search operation.
     *
     * @param connection the connection
     * @param filter     the filter
     * @return the response
     * @throws LdapException the ldap exception
     */
    Response<SearchResult> executeSearchOperation(final Connection connection, final SearchFilter filter,
                                                  final String... returnAttributes)
            throws LdapException {

        final SearchOperation searchOperation = new SearchOperation(connection);
        final SearchRequest request = newRequest(filter, returnAttributes);
        if (log.isDebugEnabled()) {
            log.debug("Using search request {}", request.toString());
        }
        return searchOperation.execute(request);
    }

    /**
     * Checks to see if response has a result.
     *
     * @param response the response
     * @return true, if successful
     */
    boolean hasResults(final Response<SearchResult> response) {
        final SearchResult result = response.getResult();
        if (result != null && result.getEntry() != null) {
            return true;
        }
        if (log.isDebugEnabled()) {
            log.debug("Requested ldap operation did not return a result or an ldap entry [code = "
                    + response.getResultCode() + ", message = " + response.getMessage() + "]");
        }
        return false;
    }

    Collection<LdapEntry> findByFilter(final String filter, final String... returnAttributes) {

        Connection connection = null;
        try {
            connection = getConnection();
            return findByFilter(connection, filter, returnAttributes);

        } catch (final LdapException e) {
            InternalServerError ise = new InternalServerError(e);
            log.error(String.format("Getting LDAP entries by filter [%s] entries failed.", filter), ise);
            throw ise;

        } finally {
            closeConnection(connection);
        }
    }

    @SuppressWarnings("WeakerAccess")
    Collection<LdapEntry> findByFilter(final Connection connection, final String filter,
                                       final String... returnAttributes) throws LdapException {

        final Response<SearchResult> response = executeSearchOperation(connection, new SearchFilter(filter), returnAttributes);
        if (hasResults(response)) {
            return response.getResult().getEntries();
        }
        return new ArrayList<>();
    }

    LdapEntry findOneByFilter(final String filter, final String... returnAttributes) {

        Connection connection = null;
        try {
            connection = getConnection();
            return findOneByFilter(connection, filter, returnAttributes);

        } catch (final LdapException e) {
            InternalServerError ise = new InternalServerError(e);
            log.error(String.format("Getting one LDAP entry by filter [%s] entries failed.", filter), ise);
            throw ise;

        } finally {
            closeConnection(connection);
        }
    }

    LdapEntry findOneByFilter(final Connection connection, final String filter, final String... returnAttributes)
            throws LdapException {

        final Response<SearchResult> response = executeSearchOperation(connection,
                new SearchFilter(filter), returnAttributes);

        if (hasResults(response)) {
            return response.getResult().getEntry();
        }
        return null;
    }

    <T> LdapEntry doSave(final Connection connection, final T entity, LdapEntry destination,
                         final LdapEntryMapper<T> ldapEntryMapper)
            throws LdapException {

        LdapEntry target = destination;
        if (destination == null) {
            target = new LdapEntry();
            ldapEntryMapper.map(entity, target);
            final AddOperation operation = new AddOperation(connection);
            operation.execute(new AddRequest(target.getDn(), target.getAttributes()));
        } else {
            LdapEntry source = new LdapEntry(target.getDn(), target.getAttributes());
            ldapEntryMapper.map(entity, source);
            ModifyOperation modify = new ModifyOperation(connection);
            ModifyRequest request = new ModifyRequest(target.getDn(), LdapEntry.computeModifications(source, target));
            modify.execute(request);
        }
        return target;
    }

}
