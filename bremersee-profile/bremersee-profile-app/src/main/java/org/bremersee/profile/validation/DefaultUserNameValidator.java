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

package org.bremersee.profile.validation;

import org.apache.commons.lang3.StringUtils;
import org.bremersee.common.exception.AlreadyExistsException;
import org.bremersee.common.exception.BadUserNameException;
import org.bremersee.profile.domain.ldap.dao.UserProfileLdapDao;
import org.bremersee.profile.domain.mongodb.repository.OAuth2ClientMongoRepository;
import org.bremersee.profile.domain.mongodb.repository.UserRegistrationMongoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * @author Christian Bremer
 */
@Component("userNameValidator")
@EnableConfigurationProperties(DefaultUserNameValidatorConfig.class)
public class DefaultUserNameValidator implements UserNameValidator {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final UserRegistrationMongoRepository userRegistrationMongoRepository;

    private final UserProfileLdapDao userProfileLdapDao;

    private final OAuth2ClientMongoRepository oAuth2ClientMongoRepository;

    private Pattern userNameRegex = Pattern.compile("^[@a-zA-Z0-9._-]{3,75}$");

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    public DefaultUserNameValidator(
            final UserRegistrationMongoRepository userRegistrationMongoRepository,
            final UserProfileLdapDao userProfileLdapDao,
            final OAuth2ClientMongoRepository oAuth2ClientMongoRepository) {

        this.userRegistrationMongoRepository = userRegistrationMongoRepository;
        this.userProfileLdapDao = userProfileLdapDao;
        this.oAuth2ClientMongoRepository = oAuth2ClientMongoRepository;
    }

    @Autowired(required = false)
    public void setConfig(final DefaultUserNameValidatorConfig config) {
        if (config != null && StringUtils.isNotBlank(config.getUserNameRegex())) {
            log.info("{} uses config {}", getClass().getSimpleName(), config);
            if (config.getUserNameRegexFlags() == null) {
                userNameRegex = Pattern.compile(config.getUserNameRegex());
            } else {
                userNameRegex = Pattern.compile(config.getUserNameRegex(), config.getUserNameRegexFlags());
            }
        }
    }

    @Override
    public void validate(final String userName) {
        if (StringUtils.isBlank(userName)) {
            throw new BadUserNameException("User name must by present.");
        }
        if (!userNameRegex.matcher(userName).matches()) {
            throw new BadUserNameException("User name contains illegal character(s) or is too short. " +
                    "The current regular expression to validate the username is '" + userNameRegex.pattern() + "'.");
        }
    }

    @Override
    public void validateNew(final String userName, int tableMask) {
        validate(userName);
        if (((tableMask
                & ValidatorConstants.USER_REGISTRATION_TABLE_MASK) == ValidatorConstants.USER_REGISTRATION_TABLE_MASK)
                && userRegistrationMongoRepository.findByUid(userName) != null) {
            throw getAlreadyExistsException(userName);
        }
        if (((tableMask & ValidatorConstants.USER_TABLE_MASK) == ValidatorConstants.USER_TABLE_MASK)
                && userProfileLdapDao.existsByUserName(userName)) {
            throw getAlreadyExistsException(userName);
        }
        if (((tableMask & ValidatorConstants.OAUTH2_CLIENT_TABLE_MASK) == ValidatorConstants.OAUTH2_CLIENT_TABLE_MASK)
                && oAuth2ClientMongoRepository.findByClientId(userName) != null) {
            throw getAlreadyExistsException(userName);
        }
    }

    private AlreadyExistsException getAlreadyExistsException(final String userName) {
        return new AlreadyExistsException("User name [" + userName + "] already exists.");
    }

}
