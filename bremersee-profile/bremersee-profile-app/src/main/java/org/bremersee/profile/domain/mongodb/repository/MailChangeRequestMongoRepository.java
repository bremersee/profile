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

import org.bremersee.profile.domain.mongodb.entity.MailChangeRequestMongo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.math.BigInteger;

/**
 * @author Christian Bremer
 */
public interface MailChangeRequestMongoRepository extends MongoRepository<MailChangeRequestMongo, BigInteger>,
        MailChangeRequestMongoRepositoryCustom {

    @Query("{ $or: [ { 'uid': { $regex: ?0 } }, { 'newEmail': { $regex: ?0 } } ] }")
    Page<MailChangeRequestMongo> findBySearchRegex(String searchRegex, Pageable pageable);

    MailChangeRequestMongo findByChangeHash(String changeHash);

    MailChangeRequestMongo findByUid(String uid);

    MailChangeRequestMongo findByNewEmail(String newEmail);

}
