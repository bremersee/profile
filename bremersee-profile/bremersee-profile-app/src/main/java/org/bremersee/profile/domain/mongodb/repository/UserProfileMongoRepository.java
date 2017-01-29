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

import org.bremersee.profile.domain.mongodb.entity.UserProfileMongo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.math.BigInteger;

/**
 * @author Christian Bremer
 */
public interface UserProfileMongoRepository extends MongoRepository<UserProfileMongo, BigInteger> {

    @Query("{ $or: [ { 'uid': { $regex: ?0 } }, { 'email': { $regex: ?0 } }, { 'firstName': { $regex: ?0 } }, { 'lastName': { $regex: ?0 } } ] }")
    Page<UserProfileMongo> findBySearchRegex(String searchRegex, Pageable pageable);

    UserProfileMongo findByUid(String uid);

    UserProfileMongo findByEmail(String email);

    UserProfileMongo findByMobile(String mobile);

    void deleteByUid(String uid);

}
