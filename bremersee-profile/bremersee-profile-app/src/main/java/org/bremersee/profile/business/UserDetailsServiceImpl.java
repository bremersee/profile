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

import org.apache.commons.lang3.Validate;
import org.bremersee.profile.domain.ldap.dao.RoleLdapDao;
import org.bremersee.profile.domain.ldap.dao.UserProfileLdapDao;
import org.bremersee.profile.domain.ldap.entity.UserProfileLdap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Christian Bremer
 */
@Service("userDetailsService")
public class UserDetailsServiceImpl implements UserDetailsService {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final RoleLdapDao roleLdapDao;

    private final UserProfileLdapDao userProfileLdapDao;

    @Autowired
    public UserDetailsServiceImpl(
            final RoleLdapDao roleLdapDao,
            final UserProfileLdapDao userProfileLdapDao) {

        this.roleLdapDao = roleLdapDao;
        this.userProfileLdapDao = userProfileLdapDao;
    }

    @Override
    public UserDetails loadUserByUsername(final String username) {

        Validate.notBlank(username, "User name must not be null or blank.");
        UserProfileLdap entity;
        if (username.contains("@")) {
            entity = userProfileLdapDao.findByEmail(username);
            if (entity == null) {
                entity = userProfileLdapDao.findByUserName(username);
            }
        } else {
            entity = userProfileLdapDao.findByUserName(username);
        }

        if (entity == null) {
            UsernameNotFoundException e = new UsernameNotFoundException("User with name [" + username
                    + "] was not found.");
            log.error("Loading user failed.", e);
            throw e;
        }

        List<GrantedAuthority> authorities = new ArrayList<>();
        Set<String> roles = roleLdapDao.findRoleNamesByMember(entity.getUid());
        for (String role : roles) {
            authorities.add(new SimpleGrantedAuthority(role));
        }

        return new User(username, entity.getPassword(), true, true, true,
                true, authorities);
    }

}
