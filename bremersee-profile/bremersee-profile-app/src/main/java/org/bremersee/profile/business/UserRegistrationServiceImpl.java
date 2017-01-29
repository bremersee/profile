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

package org.bremersee.profile.business;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.bremersee.common.exception.BadRequestException;
import org.bremersee.common.exception.NotFoundException;
import org.bremersee.common.security.core.context.RunAsCallbackWithoutResult;
import org.bremersee.fac.FailedAccessCounter;
import org.bremersee.fac.model.AccessResultDto;
import org.bremersee.pagebuilder.model.Page;
import org.bremersee.pagebuilder.model.PageRequest;
import org.bremersee.pagebuilder.model.PageRequestDto;
import org.bremersee.pagebuilder.spring.PageBuilderSpringUtils;
import org.bremersee.pagebuilder.spring.SpringPageRequest;
import org.bremersee.profile.domain.mongodb.entity.UserRegistrationMongo;
import org.bremersee.profile.domain.mongodb.mapper.UserRegistrationMongoMapper;
import org.bremersee.profile.domain.mongodb.repository.UserRegistrationMongoRepository;
import org.bremersee.profile.model.UserRegistrationDto;
import org.bremersee.profile.model.UserRegistrationRequestDto;
import org.bremersee.profile.validation.EmailValidator;
import org.bremersee.profile.validation.PasswordValidator;
import org.bremersee.profile.validation.UserNameValidator;
import org.bremersee.profile.validation.ValidatorConstants;
import org.bremersee.utils.LocaleUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import java.io.Serializable;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

/**
 * @author Christian Bremer
 */
@Service("userRegistrationService")
@EnableConfigurationProperties(UserProfileProperties.class)
public class UserRegistrationServiceImpl extends AbstractServiceImpl implements UserRegistrationService {

    private final UserRegistrationMongoMapper userRegistrationMongoMapper;

    private final UserRegistrationMongoRepository userRegistrationMongoRepository;

    private final UserProfileService userProfileService;

    private final FailedAccessCounter failedAccessCounter;

    private final PasswordValidator passwordValidator;

    private final UserNameValidator userNameValidator;

    private final EmailValidator emailValidator;

    private final JavaMailSender mailSender;

    private MessageSource messageSource;

    private UserProfileProperties userProfileProperties = new UserProfileProperties();

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    public UserRegistrationServiceImpl( // NOSONAR
                                        final UserRegistrationMongoMapper userRegistrationMongoMapper,
                                        final UserRegistrationMongoRepository userRegistrationMongoRepository,
                                        final UserProfileService userProfileService,
                                        final FailedAccessCounter failedAccessCounter,
                                        final PasswordValidator passwordValidator,
                                        final UserNameValidator userNameValidator,
                                        final EmailValidator emailValidator,
                                        final JavaMailSender mailSender) {

        this.userRegistrationMongoMapper = userRegistrationMongoMapper;
        this.userRegistrationMongoRepository = userRegistrationMongoRepository;
        this.userProfileService = userProfileService;
        this.failedAccessCounter = failedAccessCounter;
        this.passwordValidator = passwordValidator;
        this.userNameValidator = userNameValidator;
        this.emailValidator = emailValidator;
        this.mailSender = mailSender;
    }

    @Autowired(required = false)
    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Autowired(required = false)
    public void setUserProfileProperties(UserProfileProperties userProfileProperties) {
        if (userProfileProperties != null) {
            this.userProfileProperties = userProfileProperties;
        }
    }

    @Override
    protected void doInit() {
        log.info("properties = ", userProfileProperties);
    }

    @Override
    public void processRegistrationRequest(final UserRegistrationRequestDto request) {

        BadRequestException.validateNotNull(request, "User registration request must be present.");
        BadRequestException.validateNotBlank(request.getUid(), "Users user name must be present.");
        BadRequestException.validateNotBlank(request.getFirstName(), "Users first name must be present.");
        BadRequestException.validateNotBlank(request.getLastName(), "Users last name must be present.");
        BadRequestException.validateNotBlank(request.getEmail(), "Users email address must be present.");

        userNameValidator.validateNew(request.getUid(), ValidatorConstants.ALL_TABLE_MASK);
        emailValidator.validateNew(request.getEmail(), true, ValidatorConstants.ALL_TABLE_MASK);
        passwordValidator.validate(request.getPassword(), null, null, null);

        final long expirationMillis = System.currentTimeMillis()
                + userProfileProperties.getUserRegistrationLifetimeDuration().toMillis();
        UserRegistrationMongo entity = userRegistrationMongoMapper.mapToEntity(request);
        entity.setRegistrationExpiration(new Date(expirationMillis));
        entity.setRegistrationHash(UUID.randomUUID().toString());
        while (userRegistrationMongoRepository.findByRegistrationHash(entity.getRegistrationHash()) != null) {
            entity.setRegistrationHash(UUID.randomUUID().toString());
        }

        entity = userRegistrationMongoRepository.save(entity);

        sendValidationEmail(entity);
    }

    private void sendValidationEmail(final UserRegistrationMongo userRegistration) {

        MimeMessagePreparator preparator = mimeMessage -> {

            final String linkEncoding;
            if (StringUtils.isBlank(userProfileProperties.getLinkEncoding())) {
                linkEncoding = StandardCharsets.UTF_8.name();
            } else {
                linkEncoding = userProfileProperties.getLinkEncoding();
            }
            final String hash = URLEncoder.encode(userRegistration.getRegistrationHash(), linkEncoding);
            final String href = userProfileProperties.getUserRegistrationLink().replace("{registrationHash}", hash);

            mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(userRegistration.getEmail()));
            mimeMessage.setFrom(new InternetAddress(userProfileProperties.getUserRegistrationSender()));
            mimeMessage.setSubject(getSubject(userRegistration));
            mimeMessage.setText("Dear " + userRegistration.getFirstName() + " " + userRegistration.getLastName()
                    + ", welcome to bremersee.org! Please click " + href + " to complete your registration.");
        };

        this.mailSender.send(preparator);
    }

    private String getSubject(final UserRegistrationMongo userRegistration) {
        final String value;
        if (StringUtils.isBlank(userProfileProperties.getUserRegistrationSubject())) {
            value = "Welcome to bremersee.org";
        } else {
            value = userProfileProperties.getUserRegistrationSubject();
        }
        if (messageSource != null) {
            Locale locale = LocaleUtils.fromString(userRegistration.getPreferredLocale(), true);
            return messageSource.getMessage(userProfileProperties.getUserRegistrationSubjectCode(), null, value, locale);
        }
        return value;
    }

    @Override
    public AccessResultDto processRegistrationValidation(final String registrationHash, final String remoteHost) {

        BadRequestException.validateNotBlank(registrationHash, "Registration hash must be present.");
        final String host = StringUtils.isBlank(remoteHost) ? UUID.randomUUID().toString() : remoteHost;

        UserRegistrationMongo entity = userRegistrationMongoRepository.findByRegistrationHash(registrationHash);

        final boolean validEntity = entity != null
                && (entity.getRegistrationExpiration() == null
                || entity.getRegistrationExpiration().after(new Date()));

        final long ct = System.currentTimeMillis();
        final AccessResultDto accessResult;
        if (validEntity) {
            accessResult = failedAccessCounter.accessSucceeded("user-registration", host, ct);
        } else {
            accessResult = failedAccessCounter.accessFailed("user-registration", host, ct);
        }

        if (accessResult.isAccessGranted() && entity != null) {
            final UserRegistrationDto dto = userRegistrationMongoMapper.mapToDto(entity);
            dto.setPassword(entity.getPassword());
            dto.setSambaLmPassword(entity.getSambaLmPassword());
            dto.setSambaNtPassword(entity.getSambaNtPassword());
            createUserProfile(dto);
            userRegistrationMongoRepository.delete(entity);
        }

        return accessResult;
    }

    private void createUserProfile(final UserRegistrationDto userRegistration) {
        runAsSystemWithoutResult(new RunAsCallbackWithoutResult() {
            @Override
            public void run() {
                userProfileService.create(userRegistration);
            }
        });
    }

    @Override
    public Page<UserRegistrationDto> findAll(final PageRequest request) {

        final PageRequest pageRequest = request == null ? new PageRequestDto() : request;
        SpringPageRequest pageable = PageBuilderSpringUtils.toSpringPageRequest(pageRequest);
        org.springframework.data.domain.Page<UserRegistrationMongo> springPage;
        if (StringUtils.isBlank(pageRequest.getQuery())) {
            springPage = userRegistrationMongoRepository.findAll(pageable);
        } else {
            springPage = userRegistrationMongoRepository.findBySearchRegex(pageRequest.getQuery(), pageable);
        }
        return PageBuilderSpringUtils.fromSpringPage(springPage, userRegistrationMongoMapper::mapToDto);
    }

    @Override
    public UserRegistrationDto findByRegistrationHash(final String registrationHash) {

        UserRegistrationMongo entity = userRegistrationMongoRepository.findByRegistrationHash(registrationHash);
        NotFoundException.validateNotNull(entity,
                String.format("An user registration with hash [%s] was not found.", registrationHash));
        return userRegistrationMongoMapper.mapToDto(entity);
    }

    @Override
    public UserRegistrationDto findByUserName(final String userName) {

        UserRegistrationMongo entity = userRegistrationMongoRepository.findByUid(userName);
        NotFoundException.validateNotNull(entity, "An user registration with user name [" + userName + "] was not found.");
        return userRegistrationMongoMapper.mapToDto(entity);
    }

    @Override
    public UserRegistrationDto findByEmail(final String email) {

        UserRegistrationMongo entity = userRegistrationMongoRepository.findByEmail(email);
        NotFoundException.validateNotNull(entity, "An user registration with email [" + email + "] was not found.");
        return userRegistrationMongoMapper.mapToDto(entity);
    }

    @Override
    public void deleteById(final Serializable id) {

        Validate.notNull(id, "ID must be present.");
        userRegistrationMongoRepository.delete(new BigInteger(id.toString()));
    }

    @Scheduled(cron = "0 13 0 * * ?") // second, minute, hour, day of month, month, day(s) of week
    public void deleteExpired() {
        log.debug("Deleting expired user registration entries ...");
        userRegistrationMongoRepository.findExpiredAndRemove();
    }

}
