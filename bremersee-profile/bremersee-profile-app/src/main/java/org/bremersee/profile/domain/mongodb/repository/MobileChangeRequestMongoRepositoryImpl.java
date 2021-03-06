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

package org.bremersee.profile.domain.mongodb.repository;

import org.bremersee.profile.domain.mongodb.entity.MobileChangeRequestMongo;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.Date;
import java.util.List;

/**
 * @author Christian Bremer
 */
public class MobileChangeRequestMongoRepositoryImpl extends AbstractMongoRepositoryImpl
        implements MobileChangeRequestMongoRepositoryCustom {

    @Override
    protected void doInit() {
        // nothing to init
    }

    @Override
    public List<MobileChangeRequestMongo> findExpiredAndRemove() {

        Query query = new Query();
        query.addCriteria(Criteria.where("changeExpiration").lt(new Date()));
        return mongoOperations.findAllAndRemove(query, MobileChangeRequestMongo.class);
    }
}
