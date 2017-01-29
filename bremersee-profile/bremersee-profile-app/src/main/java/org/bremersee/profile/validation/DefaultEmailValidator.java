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

package org.bremersee.profile.validation;

import org.apache.commons.lang3.StringUtils;
import org.bremersee.common.exception.AlreadyExistsException;
import org.bremersee.common.exception.BadEmailAddressException;
import org.bremersee.profile.domain.ldap.dao.UserProfileLdapDao;
import org.bremersee.profile.domain.mongodb.repository.MailChangeRequestMongoRepository;
import org.bremersee.profile.domain.mongodb.repository.UserRegistrationMongoRepository;
import org.bremersee.utils.MailUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * @author Christian Bremer
 */
@Component("emailValidator")
@EnableConfigurationProperties(DefaultEmailValidatorConfig.class)
public class DefaultEmailValidator implements EmailValidator {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final UserRegistrationMongoRepository userRegistrationMongoRepository;

    private final MailChangeRequestMongoRepository mailChangeRequestMongoRepository;

    private final UserProfileLdapDao userProfileLdapDao;

    private Pattern emailRegex = Pattern.compile(MailUtils.EMAIL_REGEX);

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    public DefaultEmailValidator(
            final UserRegistrationMongoRepository userRegistrationMongoRepository,
            final MailChangeRequestMongoRepository mailChangeRequestMongoRepository,
            final UserProfileLdapDao userProfileLdapDao) {

        this.userRegistrationMongoRepository = userRegistrationMongoRepository;
        this.mailChangeRequestMongoRepository = mailChangeRequestMongoRepository;
        this.userProfileLdapDao = userProfileLdapDao;
    }

    @Autowired(required = false)
    public void setConfig(DefaultEmailValidatorConfig config) {
        if (config != null && StringUtils.isNotBlank(config.getEmailRegex())) {
            log.info("{} uses config {}", getClass().getSimpleName(), config);
            if (config.getEmailRegexFlags() == null) {
                emailRegex = Pattern.compile(config.getEmailRegex());
            } else {
                emailRegex = Pattern.compile(config.getEmailRegex(), config.getEmailRegexFlags());
            }
        }
    }

    @Override
    public void validate(final String email, boolean valueRequired) {
        if (valueRequired && StringUtils.isBlank(email)) {
            throw new BadEmailAddressException("Email address must be present.");
        }
        if (StringUtils.isNotBlank(email) && !emailRegex.matcher(email).matches()) {
            throw new BadEmailAddressException("Email contains illegal character(s). " +
                    "The current regular expression to validate the email address is '" + emailRegex.pattern() + "'.");
        }
    }

    @Override
    public void validateNew(final String email, boolean valueRequired, final int tableMask) {
        validate(email, valueRequired);
        if (StringUtils.isBlank(email)) {
            return;
        }
        if (((tableMask & ValidatorConstants.USER_REGISTRATION_TABLE_MASK) == ValidatorConstants.USER_REGISTRATION_TABLE_MASK)
                && userRegistrationMongoRepository.findByEmail(email) != null) {
            throw new AlreadyExistsException("User with email address [" + email + "] already exists."); // NOSONAR
        }
        if (((tableMask & ValidatorConstants.CHANGE_EMAIL_TABLE_MASK) == ValidatorConstants.CHANGE_EMAIL_TABLE_MASK)
                && mailChangeRequestMongoRepository.findByNewEmail(email) != null) {
            throw new AlreadyExistsException("User with email address [" + email + "] already exists."); // NOSONAR
        }
        if (((tableMask & ValidatorConstants.USER_TABLE_MASK) == ValidatorConstants.USER_TABLE_MASK)
                && userProfileLdapDao.existsByEmail(email)) {
            throw new AlreadyExistsException("User with email address [" + email + "] already exists."); // NOSONAR
        }
    }

}
