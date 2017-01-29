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
import org.bremersee.common.exception.BadPhoneNumberException;
import org.bremersee.utils.PhoneNumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * @author Christian Bremer
 */
@Component("mobileNumberValidator")
@EnableConfigurationProperties(DefaultMobileNumberValidatorConfig.class)
public class DefaultMobileNumberValidator implements MobileNumberValidator {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private Pattern mobileNumberRegex = Pattern.compile("^\\d+$");

    @Autowired(required = false)
    public void setConfig(DefaultMobileNumberValidatorConfig config) {
        if (config != null && StringUtils.isNotBlank(config.getMobileNumberRegex())) {
            log.info("{} uses config {}", getClass().getSimpleName(), config);
            if (config.getMobileNumberRegexFlags() == null) {
                mobileNumberRegex = Pattern.compile(config.getMobileNumberRegex());
            } else {
                mobileNumberRegex = Pattern.compile(config.getMobileNumberRegex(), config.getMobileNumberRegexFlags());
            }
        }
    }

    @Override
    public String validate(final String mobileNumber, boolean valueRequired) {
        if (log.isDebugEnabled()) {
            log.debug("Validating mobile number [{}] ...", mobileNumber);
        }
        final String cleanNumber = doValidate(mobileNumber, valueRequired);
        if (log.isDebugEnabled()) {
            log.debug("Mobile number [{}] successfully validated. Returning validated number [{}].",
                    mobileNumber, cleanNumber);
        }
        return cleanNumber;
    }

    private String doValidate(final String mobileNumber, boolean valueRequired) {
        if (valueRequired && StringUtils.isBlank(mobileNumber)) {
            throw new BadPhoneNumberException("Phone number must be present.");
        }
        final String cleanNumber = PhoneNumberUtils.cleanPhoneNumber(mobileNumber, false);
        if (StringUtils.isNotBlank(mobileNumber) && !mobileNumberRegex.matcher(cleanNumber).matches()) {
            throw new BadPhoneNumberException("Phone number contains illegal character(s).");
        }
        return PhoneNumberUtils.cleanPhoneNumber(mobileNumber, true);
    }

}
