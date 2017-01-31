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

import org.bremersee.common.exception.BadRequestException;
import org.bremersee.common.exception.InternalServerError;
import org.ldaptive.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Christian Bremer
 */
@Component("oAuth2ClientLdapDao")
@EnableConfigurationProperties(OAuth2ClientLdapProperties.class)
public class OAuth2ClientLdapDaoImpl extends AbstractLdapDaoImpl implements OAuth2ClientLdapDao {

    private OAuth2ClientLdapProperties properties = new OAuth2ClientLdapProperties();

    @Autowired(required = false)
    public void setProperties(OAuth2ClientLdapProperties properties) {
        this.properties = properties;
    }

    @Override
    void doInit() {
        log.info("properties = {}", properties);
    }

    @Override
    AbstractLdapProperties getProperties() {
        return properties;
    }

    private String createDn(final String clientId) {
        final String dn = String.format("%s=%s,%s", properties.getRdn(), clientId,
                properties.getSearchRequest().getBaseDn());
        log.debug("Creating DN of OAuth2 Client [{}]: {}", clientId, dn);
        return dn;
    }

    @Override
    public void save(final String clientId) {

        BadRequestException.validateNotBlank(clientId, "OAuth2 client ID must be present."); // NOSONAR
        Connection connection = null;
        try {
            connection = getConnection();

            LdapEntry target = findLdapEntryByClientId(connection, clientId);
            if (target == null) {
                target = new LdapEntry();
                target.setDn(createDn(clientId));
                target.addAttribute(new LdapAttribute("objectClass", "top", "uidObject", "namedObject"));
                target.addAttribute(new LdapAttribute("uid", clientId));
                target.addAttribute(new LdapAttribute("cn", clientId));
                final AddOperation operation = new AddOperation(connection);
                operation.execute(new AddRequest(target.getDn(), target.getAttributes()));
            }

        } catch (final LdapException e) {

            InternalServerError ise = new InternalServerError(e);
            if (log.isErrorEnabled()) {
                log.error(String.format("Saving OAuth2 client [%s] failed.", clientId), ise);
            }
            throw ise;

        } finally {
            closeConnection(connection);
        }
    }

    @Override
    public void delete(final String clientId) {

        BadRequestException.validateNotBlank(clientId, "OAuth2 client ID must be present.");
        Connection connection = null;
        try {
            connection = getConnection();
            final DeleteOperation delete = new DeleteOperation(connection);
            final DeleteRequest request = new DeleteRequest(createDn(clientId));
            final Response<Void> res = delete.execute(request);
            boolean result = res.getResultCode() == ResultCode.SUCCESS;
            if (log.isDebugEnabled()) {
                log.debug("OAuth2 Client [{}] "
                        + (result ? "successfully removed." : "doesn't exist. So it can't be removed."), clientId);
            }

        } catch (final LdapException e) {

            InternalServerError ise = new InternalServerError(e);
            log.error("Deleting of OAuth2 client [" + clientId + "] failed.", ise);
            throw ise;

        } finally {
            closeConnection(connection);
        }
    }

    @Override
    public boolean exists(final String clientId) {

        BadRequestException.validateNotBlank(clientId, "OAuth2 client ID must be present.");
        return findLdapEntryByClientId(clientId) != null;
    }

    private LdapEntry findLdapEntryByClientId(final String clientId) {
        final String filter = String.format("(&(objectClass=namedObject)(objectClass=uidObject)(uid=%s))", clientId);
        return findOneByFilter(filter);
    }

    private LdapEntry findLdapEntryByClientId(final Connection connection, final String clientId) throws LdapException {
        final String filter = String.format("(&(objectClass=namedObject)(objectClass=uidObject)(uid=%s))", clientId);
        return findOneByFilter(connection, filter);
    }

}
