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
import org.bremersee.pagebuilder.model.Page;
import org.bremersee.pagebuilder.model.PageRequest;
import org.bremersee.pagebuilder.model.PageRequestDto;
import org.bremersee.pagebuilder.spring.PageBuilderSpringUtils;
import org.bremersee.pagebuilder.spring.SpringPageRequest;
import org.bremersee.profile.domain.mongodb.entity.MobileChangeRequestMongo;
import org.bremersee.profile.domain.mongodb.mapper.MobileChangeRequestMongoMapper;
import org.bremersee.profile.domain.mongodb.repository.MobileChangeRequestMongoRepository;
import org.bremersee.profile.model.MobileChangeRequestDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * @author Christian Bremer
 */
@Service("mobileChangeRequestService")
public class MobileChangeRequestServiceImpl extends AbstractServiceImpl implements MobileChangeRequestService {

    private final MobileChangeRequestMongoRepository mailChangeRequestMongoRepository;

    private final MobileChangeRequestMongoMapper mailChangeRequestMongoMapper;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    public MobileChangeRequestServiceImpl(
            final MobileChangeRequestMongoRepository mailChangeRequestMongoRepository,
            final MobileChangeRequestMongoMapper mailChangeRequestMongoMapper) {
        this.mailChangeRequestMongoRepository = mailChangeRequestMongoRepository;
        this.mailChangeRequestMongoMapper = mailChangeRequestMongoMapper;
    }

    @Override
    protected void doInit() {
        // nothing to init
    }

    @Override
    public Page<MobileChangeRequestDto> findAll(final PageRequest request) {
        final PageRequest pageRequest = request == null ? new PageRequestDto() : request;
        log.info("{}: Find all mail change requests with page request [{}] ...", getCurrentUserName(), pageRequest);
        final SpringPageRequest pageable = PageBuilderSpringUtils.toSpringPageRequest(pageRequest);
        org.springframework.data.domain.Page<MobileChangeRequestMongo> springPage;
        if (StringUtils.isBlank(pageRequest.getQuery())) {
            springPage = mailChangeRequestMongoRepository.findAll(pageable);
        } else {
            springPage = mailChangeRequestMongoRepository.findBySearchRegex(pageRequest.getQuery(), pageable);
        }
        return PageBuilderSpringUtils.fromSpringPage(springPage, mailChangeRequestMongoMapper::mapToDto);
    }

    @Scheduled(cron = "0 20 0 * * ?") // second, minute, hour, day of month, month, day(s) of week
    public void deleteExpired() {
        log.debug("Deleting expired user registration entries ...");
        mailChangeRequestMongoRepository.findExpiredAndRemove();
    }

}
