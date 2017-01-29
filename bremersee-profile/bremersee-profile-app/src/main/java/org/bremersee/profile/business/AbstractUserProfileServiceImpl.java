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
import org.bremersee.common.exception.BadRequestException;
import org.bremersee.common.exception.NotFoundException;
import org.bremersee.profile.domain.ldap.dao.UserProfileLdapDao;
import org.bremersee.profile.domain.ldap.entity.UserProfileLdap;
import org.bremersee.profile.domain.ldap.mapper.UserProfileLdapMapper;
import org.bremersee.profile.domain.mongodb.entity.UserProfileMongo;
import org.bremersee.profile.domain.mongodb.mapper.UserProfileMongoMapper;
import org.bremersee.profile.domain.mongodb.repository.UserProfileMongoRepository;
import org.bremersee.profile.model.UserProfileDto;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Christian Bremer
 */
public abstract class AbstractUserProfileServiceImpl extends AbstractServiceImpl {

    static final String USER_NAME_MUST_BE_PRESENT = "User name must be present.";

    static final String EMAIL_NAME_MUST_BE_PRESENT = "Email address must be present.";

    final UserProfileLdapDao userProfileLdapDao;

    final UserProfileLdapMapper userProfileLdapMapper;

    final UserProfileMongoRepository userProfileMongoRepository;

    final UserProfileMongoMapper userProfileMongoMapper;

    UserProfileProperties userProfileProperties = new UserProfileProperties();

    public AbstractUserProfileServiceImpl(
            final UserProfileLdapDao userProfileLdapDao,
            final UserProfileLdapMapper userProfileLdapMapper,
            final UserProfileMongoRepository userProfileMongoRepository,
            final UserProfileMongoMapper userProfileMongoMapper) {

        this.userProfileLdapDao = userProfileLdapDao;
        this.userProfileLdapMapper = userProfileLdapMapper;
        this.userProfileMongoRepository = userProfileMongoRepository;
        this.userProfileMongoMapper = userProfileMongoMapper;
        Validate.notNull(this.userProfileLdapDao, "userProfileLdapDao must not be null");
        Validate.notNull(this.userProfileLdapMapper, "userProfileLdapMapper must not be null");
        Validate.notNull(this.userProfileMongoRepository, "userProfileMongoRepository must not be null");
        Validate.notNull(this.userProfileMongoMapper, "userProfileMongoMapper must not be null");
    }

    @Autowired(required = false)
    public void setUserProfileProperties(UserProfileProperties userProfileProperties) {
        if (userProfileProperties != null) {
            this.userProfileProperties = userProfileProperties;
        }
    }

    UserProfileLdap loadUserProfileLdap(final String userName) {
        BadRequestException.validateNotBlank(userName, USER_NAME_MUST_BE_PRESENT);
        UserProfileLdap entity = userProfileLdapDao.findByUserName(userName);
        NotFoundException.validateNotNull(entity, String.format("User with name [%s] was not found.", userName));
        return entity;
    }

    UserProfileMongo loadUserProfileMongo(final String userName) {
        BadRequestException.validateNotBlank(userName, USER_NAME_MUST_BE_PRESENT);
        UserProfileMongo entity = userProfileMongoRepository.findByUid(userName);
        if (entity == null) {
            UserProfileLdap ldap = loadUserProfileLdap(userName);
            entity = findOrCreateUserProfileMongo(ldap);
        }
        return entity;
    }

    UserProfileMongo findOrCreateUserProfileMongo(final UserProfileLdap ldap) {
        Validate.notNull(ldap, "User profile ldap entity must not be null.");
        UserProfileMongo mongo = userProfileMongoRepository.findByUid(ldap.getUid());
        if (mongo != null) {
            return mongo;
        }
        if (log.isDebugEnabled()) {
            log.debug("{}: Mongo entity of user [{}] was not found - creating it from ldap entity [{}] ...",
                    getCurrentUserName(), ldap.getUid(), ldap);
        }
        mongo = runAsSystem(() -> {
            UserProfileDto dto = new UserProfileDto();
            userProfileLdapMapper.mapToDto(ldap, dto);
            UserProfileMongo mongo1 = new UserProfileMongo();
            userProfileMongoMapper.updateEntity(dto, mongo1);
            mongo1.setUid(ldap.getUid());
            return mongo1;
        });
        mongo = userProfileMongoRepository.save(mongo);
        if (log.isDebugEnabled()) {
            log.debug("{}: User profile mongo entity successfully created: {}", getCurrentUserName(), mongo);
        }
        return mongo;
    }

}
