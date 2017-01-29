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

import org.apache.commons.lang3.StringUtils;
import org.bremersee.common.exception.BadRequestException;
import org.bremersee.common.exception.NotFoundException;
import org.bremersee.fac.FailedAccessCounter;
import org.bremersee.fac.model.AccessResultDto;
import org.bremersee.profile.domain.ldap.dao.UserProfileLdapDao;
import org.bremersee.profile.domain.ldap.entity.UserProfileLdap;
import org.bremersee.profile.domain.ldap.mapper.UserProfileLdapMapper;
import org.bremersee.profile.domain.mongodb.entity.MailChangeRequestMongo;
import org.bremersee.profile.domain.mongodb.entity.UserProfileMongo;
import org.bremersee.profile.domain.mongodb.mapper.UserProfileMongoMapper;
import org.bremersee.profile.domain.mongodb.repository.MailChangeRequestMongoRepository;
import org.bremersee.profile.domain.mongodb.repository.UserProfileMongoRepository;
import org.bremersee.profile.validation.EmailValidator;
import org.bremersee.profile.validation.ValidatorConstants;
import org.bremersee.utils.LocaleUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

/**
 * @author Christian Bremer
 */
@Service("changeEmailService")
@EnableConfigurationProperties(UserProfileProperties.class)
public class ChangeEmailServiceImpl extends AbstractUserProfileServiceImpl implements ChangeEmailService {

    private final MailChangeRequestMongoRepository mailChangeRequestMongoRepository;

    private final EmailValidator emailValidator;

    private final JavaMailSender mailSender;

    private final FailedAccessCounter failedAccessCounter;

    private MessageSource messageSource;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    public ChangeEmailServiceImpl( // NOSONAR
                                   final UserProfileLdapDao userProfileLdapDao,
                                   final UserProfileLdapMapper userProfileLdapMapper,
                                   final UserProfileMongoRepository userProfileMongoRepository,
                                   final UserProfileMongoMapper userProfileMongoMapper,

                                   final MailChangeRequestMongoRepository mailChangeRequestMongoRepository,
                                   final EmailValidator emailValidator,
                                   final JavaMailSender mailSender,
                                   final FailedAccessCounter failedAccessCounter) {

        super(userProfileLdapDao, userProfileLdapMapper, userProfileMongoRepository, userProfileMongoMapper);
        this.mailChangeRequestMongoRepository = mailChangeRequestMongoRepository;
        this.emailValidator = emailValidator;
        this.mailSender = mailSender;
        this.failedAccessCounter = failedAccessCounter;
    }

    @Autowired(required = false)
    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    protected void doInit() {
        // nothing to log
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SYSTEM')"
            + " or hasPermission(#userName, 'UserProfile', 'write')")
    @Override
    public void changeEmail(final String userName, final String newEmail) {

        log.info("{}: Setting new email [{}] of user [{}] ...", getCurrentUserName(), newEmail, userName);
        BadRequestException.validateNotBlank(newEmail, EMAIL_NAME_MUST_BE_PRESENT);
        UserProfileLdap userProfileLdap = loadUserProfileLdap(userName);
        if (newEmail.equals(userProfileLdap.getEmail())) {
            log.info("{}: Setting new email [{}] of user [{}]: DONE (old and new email are equal)!",
                    getCurrentUserName(), newEmail, userName);
            return;
        }
        UserProfileMongo userProfileMongo = findOrCreateUserProfileMongo(userProfileLdap);

        emailValidator.validateNew(newEmail, true, ValidatorConstants.ALL_TABLE_MASK);

        if (isCurrentUserAdminOrSystem()) {
            userProfileMongo.setEmail(newEmail);
            userProfileMongoRepository.save(userProfileMongo);
            userProfileLdap.setEmail(newEmail);
            userProfileLdapDao.save(userProfileLdap);

        } else {

            MailChangeRequestMongo request = mailChangeRequestMongoRepository.findByUid(userName);
            if (request == null) {
                request = new MailChangeRequestMongo();
            }
            request.setChangeExpiration(new Date(System.currentTimeMillis()
                    + userProfileProperties.getChangeEmailLifetimeDuration().toMillis()));
            request.setChangeHash(UUID.randomUUID().toString());
            request.setNewEmail(newEmail);
            request.setUid(userName);
            while (mailChangeRequestMongoRepository.findByChangeHash(request.getChangeHash()) != null) {
                request.setChangeHash(UUID.randomUUID().toString());
            }
            request = mailChangeRequestMongoRepository.save(request);
            sendChangeEmailMessage(request, userProfileMongo);
        }
        log.info("{}: Setting new email [{}] of user [{}]: DONE!", getCurrentUserName(), newEmail, userName);
    }

    private void sendChangeEmailMessage(final MailChangeRequestMongo request, final UserProfileMongo userProfile) {

        MimeMessagePreparator mimeMessagePreparator = mimeMessage -> {

            final String hash = URLEncoder.encode(request.getChangeHash(),
                    StringUtils.isBlank(userProfileProperties.getLinkEncoding()) ?
                            "UTF-8" : userProfileProperties.getLinkEncoding());
            final String href = userProfileProperties.getChangeEmailLink().replace("{changeHash}", hash);

            mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(request.getNewEmail()));
            mimeMessage.setFrom(new InternetAddress(userProfileProperties.getChangeEmailSender()));
            mimeMessage.setSubject(getSubject(userProfile));
            mimeMessage.setText("Dear " + userProfile.getFirstName() + " " + userProfile.getLastName()
                    + ", please validate your new email address! Click " + href
                    + " to complete the change of your email address.");
        };

        this.mailSender.send(mimeMessagePreparator);
    }

    private String getSubject(final UserProfileMongo userProfile) {
        final String value = StringUtils.isBlank(userProfileProperties.getChangeEmailSubject()) ?
                "Validate new email address" : userProfileProperties.getChangeEmailSubject();
        if (messageSource != null) {
            Locale locale = LocaleUtils.fromString(userProfile.getPreferredLocale(), true);
            return messageSource.getMessage(userProfileProperties.getChangeEmailSubjectCode(), null, value, locale);
        }
        return value;
    }

    @Override
    public AccessResultDto changeEmailByChangeHash(final String changeHash, final String remoteHost) {

        log.info("{}: Change email by hash [{}] and host [{}] ...", getCurrentUserName(), changeHash, remoteHost);

        BadRequestException.validateNotBlank(changeHash, "Hash must be present.");
        final String host = StringUtils.isBlank(remoteHost) ? UUID.randomUUID().toString() : remoteHost;

        MailChangeRequestMongo entity = mailChangeRequestMongoRepository.findByChangeHash(changeHash);

        final boolean validEntity = entity != null
                && (entity.getChangeExpiration() == null || entity.getChangeExpiration().after(new Date()));

        final long ct = System.currentTimeMillis();
        final AccessResultDto accessResult;
        if (validEntity) {
            accessResult = failedAccessCounter.accessSucceeded("change-email", host, ct);
        } else {
            accessResult = failedAccessCounter.accessFailed("change-email", host, ct);
        }

        if (accessResult.isAccessGranted() && entity != null) {
            UserProfileLdap userProfileLdap = userProfileLdapDao.findByUserName(entity.getUid());
            NotFoundException.validateNotNull(userProfileLdap,
                    String.format("User profile with name [%s] was not found.", entity.getUid())); // NOSONAR

            UserProfileMongo userProfileMongo = findOrCreateUserProfileMongo(userProfileLdap);

            userProfileMongo.setEmail(entity.getNewEmail());
            userProfileMongoRepository.save(userProfileMongo);
            userProfileLdap.setEmail(entity.getNewEmail());
            userProfileLdapDao.save(userProfileLdap);

            mailChangeRequestMongoRepository.delete(entity);
        }

        log.info("{}: Change email by hash [{}] and host [{}]: DONE [{}]!",
                getCurrentUserName(), changeHash, remoteHost, accessResult);
        return accessResult;
    }

}
