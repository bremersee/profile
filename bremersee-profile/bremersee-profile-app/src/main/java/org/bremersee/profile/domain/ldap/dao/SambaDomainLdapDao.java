/*
 * Copyright 2016 the original author or authors.
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

import org.bremersee.pagebuilder.model.Page;
import org.bremersee.pagebuilder.model.PageRequest;
import org.bremersee.profile.domain.ldap.entity.SambaDomainLdap;

/**
 * @author Christian Bremer
 */
public interface SambaDomainLdapDao {

    SambaDomainLdap save(SambaDomainLdap entity);

    Page<SambaDomainLdap> findAll(PageRequest pageRequest);

    SambaDomainLdap findBySambaDomainName(String sambaDomainName);

    SambaDomainLdap findBySambaSID(String sambaSID);

    SambaDomainLdap findBySambaDomainNameOrSambaSID(String sambaDomainNameOrSambaSID);

    boolean existsBySambaDomainName(String sambaDomainName);

    boolean existsBySambaSID(String sambaSID);

    boolean existsBySambaDomainNameOrSambaSID(String sambaDomainNameOrSambaSID);

    void deleteBySambaDomainName(String sambaDomainName);


}
