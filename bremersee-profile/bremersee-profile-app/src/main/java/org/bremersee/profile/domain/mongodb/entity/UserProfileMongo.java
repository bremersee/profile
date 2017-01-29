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

package org.bremersee.profile.domain.mongodb.entity;

import lombok.*;
import org.bremersee.common.domain.mongodb.entity.AbstractBaseMongo;
import org.bremersee.common.model.Gender;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Locale;
import java.util.TimeZone;

/**
 * @author Christian Bremer
 */
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Document(collection = "userProfile")
public class UserProfileMongo extends AbstractBaseMongo {

    private static final long serialVersionUID = 1L;

    @Indexed(unique = true)
    private String uid;

    private Gender gender;

    private String title; // personalTitle or academicTitle

    @Indexed
    private String firstName;

    @Indexed
    private String lastName;

    private String dateOfBirth; // format yyyy-MM-dd

    private String preferredLocale = Locale.getDefault().toString();

    private String preferredTimeZoneID = TimeZone.getDefault().getID();

    @Indexed(unique = true)
    private String email;

    @Indexed(unique = true, sparse = true)
    private String mobile;

    private String homePhone;

    private String homePostalAddress;

    private String location; //l

    private String state; //st

    private String labeledURI;

}
