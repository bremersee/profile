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

package org.bremersee.profile.domain.acls.model;

import org.bremersee.common.security.acls.model.ObjectIdentityRetrievalStrategyInterceptor;
import org.bremersee.common.security.acls.model.ObjectIdentityUtils;
import org.bremersee.profile.domain.ldap.entity.SambaDomainLdap;
import org.bremersee.profile.model.SambaDomainDto;
import org.springframework.security.acls.domain.IdentityUnavailableException;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * @author Christian Bremer
 */
@Component("sambaDomainAclInterceptor")
public class SambaDomainInterceptor implements ObjectIdentityRetrievalStrategyInterceptor {

    @Override
    public boolean supportsType(final String type) {
        return ObjectIdentityUtils.supportsAclType(type, SambaDomainDto.TYPE_ID, SambaDomainDto.class.getName(),
                SambaDomainDto.class.getSimpleName(),
                SambaDomainLdap.class.getName(), SambaDomainLdap.class.getSimpleName());
    }

    @Override
    public boolean supportsDomainObject(final Object domainObject) {
        return domainObject != null && supportsType(domainObject.getClass().getName());
    }

    @Override
    public ObjectIdentity getObjectIdentity(final Object domainObject) {
        if (domainObject instanceof SambaDomainDto) {
            return new ObjectIdentityImpl(SambaDomainDto.TYPE_ID, ((SambaDomainDto) domainObject).getSambaDomainName());
        }
        throw new IdentityUnavailableException("Domain object [" + domainObject + "] is not a role.");
    }

    @Override
    public ObjectIdentity createObjectIdentity(final Serializable id, final String type) {
        return new ObjectIdentityImpl(type, id.toString());
    }
}
