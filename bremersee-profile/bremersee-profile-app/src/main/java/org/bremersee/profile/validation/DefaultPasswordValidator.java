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
import org.bremersee.common.exception.PasswordAlreadyUsedException;
import org.bremersee.common.exception.PasswordTooWeakException;
import org.bremersee.common.exception.PasswordsNotMatchException;
import org.bremersee.common.security.crypto.password.PasswordEncoder;
import org.bremersee.common.security.crypto.password.PasswordEncoderSpringImpl;
import org.bremersee.utils.PasswordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Collection;

/**
 * @author Christian Bremer
 */
@Component("passwordValidator")
@EnableConfigurationProperties(DefaultPasswordValidatorConfig.class)
public class DefaultPasswordValidator implements PasswordValidator {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private PasswordEncoder passwordEncoder = new PasswordEncoderSpringImpl();

    private DefaultPasswordValidatorConfig config = new DefaultPasswordValidatorConfig();

    @Autowired(required = false)
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        if (passwordEncoder != null) {
            this.passwordEncoder = passwordEncoder;
        }
    }

    @Autowired(required = false)
    public void setConfig(DefaultPasswordValidatorConfig config) {
        if (config != null) {
            this.config = config;
        }
    }

    @PostConstruct
    public void init() {
        log.info("Initializing {}", getClass().getSimpleName());
        log.info("config = {}", config);
        log.info("{} successfully initialized.", getClass().getSimpleName());
    }

    @Override
    public void validate(final String newPassword,
                         final String oldEncryptedPassword,
                         final String oldPassword,
                         final Collection<String> passwordHistory) {

        boolean continueValidation = validateLength(newPassword);
        if (!continueValidation) {
            log.debug("The validation of the password was interrupted after the validation of the password length.");
            return;
        }
        validateOldPassword(oldEncryptedPassword, oldPassword);
        validatePasswordQuality(newPassword);
        validatePasswordHistory(newPassword, passwordHistory);
    }

    /**
     * Validate the length of the password. Return {@code true} if the validation of the password can continue
     * otherwise {@code false}.
     *
     * @param newPassword the new clear password
     * @return {@code false} if the password length is {@code 0} and this is allowed and no further validation is
     * necessary, otherwise {@code true}
     */
    private boolean validateLength(final String newPassword) {
        if (StringUtils.isBlank(newPassword) && config.getMinLength() > 0) {
            final int len = StringUtils.isBlank(newPassword) ? 0 : newPassword.trim().length();
            PasswordTooWeakException exc = new PasswordTooWeakException(
                    String.format("The minimum length of the password must be %d", config.getMinLength()));
            log.debug("Password is too short (length = {}).", len);
            throw exc;
        } else if (StringUtils.isBlank(newPassword)) {
            log.warn("Password is blank (which is currently allowed!)");
            return false;
        }
        return true;
    }

    private void validateOldPassword(final String oldEncryptedPassword, final String oldPassword) {
        if (StringUtils.isNotBlank(oldPassword) && StringUtils.isNotBlank(oldEncryptedPassword)
                && !passwordEncoder.matches(oldPassword, oldEncryptedPassword)) {
            PasswordsNotMatchException exc = new PasswordsNotMatchException(
                    "Old password does not match the existing one.");
            log.debug("Old password does not match the existing one.");
            throw exc;
        }
    }

    private void validatePasswordQuality(final String newPassword) {
        final double passwordQuality = PasswordUtils.getPasswordQuality(newPassword, config.getMinLength());
        if (passwordQuality < config.getMinQuality()) {
            PasswordTooWeakException exc = new PasswordTooWeakException();
            log.debug("Password is too weak (quality = {}).", passwordQuality);
            throw exc;
        }
    }

    private void validatePasswordHistory(final String newPassword, final Collection<String> passwordHistory) {
        if (passwordHistory != null && passwordHistory.contains(passwordEncoder.encode(newPassword))) {
            PasswordAlreadyUsedException exc = new PasswordAlreadyUsedException();
            log.debug("Password was already used.");
            throw exc;
        }
    }

}
