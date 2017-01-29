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

package org.bremersee.profile.domain.mongodb.mapper;

import org.bremersee.profile.domain.mongodb.entity.MailChangeRequestMongo;
import org.bremersee.profile.model.MailChangeRequestDto;
import org.springframework.stereotype.Component;

/**
 * @author Christian Bremer
 */
@Component("mailChangeRequestMongoMapper")
public class MailChangeRequestMongoMapperImpl extends AbstractMongoMapperImpl implements MailChangeRequestMongoMapper {

    @Override
    protected void doInit() {
        // nothing to init
    }

    @Override
    public void mapToDto(final MailChangeRequestMongo source, final MailChangeRequestDto destination) {
        mapMongoToBase(source, destination);
        destination.setUid(source.getUid());
        destination.setChangeExpiration(source.getChangeExpiration());
        destination.setChangeHash(source.getChangeHash());
        destination.setNewEmail(source.getNewEmail());
    }

    @Override
    public MailChangeRequestDto mapToDto(final MailChangeRequestMongo source) {
        MailChangeRequestDto destination = new MailChangeRequestDto();
        mapToDto(source, destination);
        return destination;
    }

    @Override
    public void updateEntity(final MailChangeRequestDto source, final MailChangeRequestMongo destination) {
        mapBaseToMongo(source, destination);
        destination.setChangeExpiration(source.getChangeExpiration());
    }

}
