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

import org.bremersee.profile.domain.mongodb.entity.MobileChangeRequestMongo;
import org.bremersee.profile.model.MobileChangeRequestDto;
import org.springframework.stereotype.Component;

/**
 * @author Christian Bremer
 */
@Component("mobileChangeRequestMongoMapper")
public class MobileChangeRequestMongoMapperImpl extends AbstractMongoMapperImpl
        implements MobileChangeRequestMongoMapper {

    @Override
    protected void doInit() {
        // nothing to init
    }

    @Override
    public void mapToDto(final MobileChangeRequestMongo source, final MobileChangeRequestDto destination) {
        mapMongoToBase(source, destination);
        destination.setUid(source.getUid());
        destination.setChangeExpiration(source.getChangeExpiration());
        destination.setChangeHash(source.getChangeHash());
        destination.setNewMobile(source.getNewMobile());
    }

    @Override
    public MobileChangeRequestDto mapToDto(final MobileChangeRequestMongo source) {
        MobileChangeRequestDto destination = new MobileChangeRequestDto();
        mapToDto(source, destination);
        return destination;
    }

    @Override
    public void updateEntity(final MobileChangeRequestDto source, final MobileChangeRequestMongo destination) {
        mapBaseToMongo(source, destination);
        destination.setChangeExpiration(source.getChangeExpiration());
    }

}
