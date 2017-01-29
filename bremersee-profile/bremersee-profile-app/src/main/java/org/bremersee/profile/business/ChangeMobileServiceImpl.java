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
import org.bremersee.profile.domain.mongodb.entity.MobileChangeRequestMongo;
import org.bremersee.profile.domain.mongodb.entity.UserProfileMongo;
import org.bremersee.profile.domain.mongodb.mapper.UserProfileMongoMapper;
import org.bremersee.profile.domain.mongodb.repository.MobileChangeRequestMongoRepository;
import org.bremersee.profile.domain.mongodb.repository.UserProfileMongoRepository;
import org.bremersee.profile.validation.MobileNumberValidator;
import org.bremersee.sms.SmsService;
import org.bremersee.utils.PasswordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

/**
 * @author Christian Bremer
 */
@Service("changeMobileService")
public class ChangeMobileServiceImpl extends AbstractUserProfileServiceImpl implements ChangeMobileService {

    private final MobileChangeRequestMongoRepository mobileChangeRequestMongoRepository;

    private final MobileNumberValidator mobileNumberValidator;

    private final SmsService smsService;

    private final FailedAccessCounter failedAccessCounter;

    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private MessageSource messageSource;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    public ChangeMobileServiceImpl( // NOSONAR
                                    final UserProfileLdapDao userProfileLdapDao,
                                    final UserProfileLdapMapper userProfileLdapMapper,
                                    final UserProfileMongoRepository userProfileMongoRepository,
                                    final UserProfileMongoMapper userProfileMongoMapper,

                                    final MobileChangeRequestMongoRepository mobileChangeRequestMongoRepository,
                                    final MobileNumberValidator mobileNumberValidator,
                                    final SmsService smsService,
                                    final FailedAccessCounter failedAccessCounter) {

        super(userProfileLdapDao, userProfileLdapMapper, userProfileMongoRepository, userProfileMongoMapper);
        this.mobileChangeRequestMongoRepository = mobileChangeRequestMongoRepository;
        this.mobileNumberValidator = mobileNumberValidator;
        this.smsService = smsService;
        this.failedAccessCounter = failedAccessCounter;
    }

    @Autowired(required = false)
    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    protected void doInit() {
        // nothing to init
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SYSTEM')"
            + " or hasPermission(#userName, 'UserProfile', 'write')")
    @Override
    public String changeMobile(final String userName, final String newMobile) {

        log.info("{}: Setting new mobile [{}] of user [{}] ...", getCurrentUserName(), newMobile, userName);
        BadRequestException.validateNotBlank(newMobile, "New mobile number must be present.");
        UserProfileLdap userProfileLdap = loadUserProfileLdap(userName);
        if (newMobile.equals(userProfileLdap.getMobile())) {
            log.info("{}: Setting new mobile [{}] of user [{}]: DONE (old and new mobile are equal)!",
                    getCurrentUserName(), newMobile, userName);
            return newMobile;
        }
        UserProfileMongo userProfileMongo = findOrCreateUserProfileMongo(userProfileLdap);

        final String cleanMobile = mobileNumberValidator.validate(newMobile, true);

        if (isCurrentUserAdminOrSystem()) {
            userProfileMongo.setMobile(cleanMobile);
            userProfileMongoRepository.save(userProfileMongo);
            userProfileLdap.setMobile(cleanMobile);
            userProfileLdapDao.save(userProfileLdap);

        } else {

            MobileChangeRequestMongo entity = mobileChangeRequestMongoRepository.findByUid(userName);
            if (entity == null) {
                entity = new MobileChangeRequestMongo();
            }
            entity.setChangeHash(PasswordUtils.createRandomClearPassword(6, false, false));
            entity.setChangeExpiration(new Date(System.currentTimeMillis()
                    + userProfileProperties.getChangeMobileLifetimeDuration().toMillis()));
            entity.setNewMobile(cleanMobile);
            entity.setUid(userName);
            while (mobileChangeRequestMongoRepository.findByChangeHash(entity.getChangeHash()) != null) {
                entity.setChangeHash(UUID.randomUUID().toString());
            }
            entity = mobileChangeRequestMongoRepository.save(entity);
            sendChangeMobileSms(entity, userProfileMongo);
        }

        log.info("{}: Setting new mobile [{}] of user [{}]: DONE!", getCurrentUserName(), cleanMobile, userName);
        return cleanMobile;
    }

    @SuppressWarnings("unused")
    private void sendChangeMobileSms(final MobileChangeRequestMongo request, final UserProfileMongo userProfile) {
        smsService.sendSms("bremersee", request.getNewMobile(), "Hash: " + request.getChangeHash());
    }

    @Override
    public AccessResultDto changeMobileByChangeHash(final String changeHash, final String remoteHost) {

        log.info("{}: Change mobile by hash [{}] and host [{}] ...", getCurrentUserName(), changeHash, remoteHost);

        BadRequestException.validateNotBlank(changeHash, "Hash must be present.");
        final String host = StringUtils.isBlank(remoteHost) ? UUID.randomUUID().toString() : remoteHost;

        MobileChangeRequestMongo entity = mobileChangeRequestMongoRepository.findByChangeHash(changeHash);

        final boolean validEntity = entity != null
                && (entity.getChangeExpiration() == null || entity.getChangeExpiration().after(new Date()));

        final long ct = System.currentTimeMillis();
        final AccessResultDto accessResult;
        if (validEntity) {
            accessResult = failedAccessCounter.accessSucceeded("change-mobile", host, ct);
        } else {
            accessResult = failedAccessCounter.accessFailed("change-mobile", host, ct);
        }

        if (accessResult.isAccessGranted() && entity != null) {
            UserProfileLdap userProfileLdap = userProfileLdapDao.findByUserName(entity.getUid());
            NotFoundException.validateNotNull(userProfileLdap,
                    String.format("User profile with name [%s] was not found.", entity.getUid()));

            UserProfileMongo userProfileMongo = findOrCreateUserProfileMongo(userProfileLdap);

            userProfileMongo.setMobile(entity.getNewMobile());
            userProfileMongoRepository.save(userProfileMongo);
            userProfileLdap.setMobile(entity.getNewMobile());
            userProfileLdapDao.save(userProfileLdap);

            mobileChangeRequestMongoRepository.delete(entity);
        }

        log.info("{}: Change mobile by hash [{}] and host [{}]: DONE [{}]!",
                getCurrentUserName(), changeHash, remoteHost, accessResult);
        return accessResult;
    }

}
