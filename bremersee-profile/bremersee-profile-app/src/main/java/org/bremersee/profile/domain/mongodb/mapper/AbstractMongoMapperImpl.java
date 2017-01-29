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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.bremersee.common.domain.mongodb.entity.AbstractBaseMongo;
import org.bremersee.common.exception.InternalServerError;
import org.bremersee.common.model.AbstractBaseDto;
import org.bremersee.profile.domain.AbstractDomainComponentImpl;

import java.math.BigInteger;

/**
 * @author Christian Bremer
 */
public abstract class AbstractMongoMapperImpl extends AbstractDomainComponentImpl {

    void mapMongoToBase(AbstractBaseMongo source, AbstractBaseDto destination) {
        Validate.notNull(source, "Source must not be null.");
        Validate.notNull(destination, "Destination must not be null.");
        destination.setCreated(source.getCreated());
        destination.setId(source.getId() == null ? null : source.getId().toString());
        destination.setModified(source.getCreated());
    }

    void mapBaseToMongo(AbstractBaseDto source, AbstractBaseMongo destination) {
        Validate.notNull(source, "Source must not be null.");
        Validate.notNull(destination, "Destination must not be null.");
        if (StringUtils.isNotBlank(source.getId())) {
            try {
                destination.setId(new BigInteger(source.getId()));
            } catch (NumberFormatException e) {
                InternalServerError ise = new InternalServerError(e);
                log.error("Mapping DTO to entity failed.", ise);
                throw ise;
            }
        }
    }

}
