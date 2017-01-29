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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * @author Christian Bremer
 *
 */
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Document(collection = "userRegistration")
public class UserRegistrationMongo extends AbstractBaseMongo {
    
    private static final long serialVersionUID = 1L;
    
    public static final String DATE_OF_BIRTH_PATTERN = "yyyy-MM-dd";
    
    protected static final SimpleDateFormat DATE_OF_BIRTH_SDF;
    
    static {
        SimpleDateFormat tmpSdf = new SimpleDateFormat(DATE_OF_BIRTH_PATTERN);
        DATE_OF_BIRTH_SDF = tmpSdf;
    }
    
    @Indexed(unique = true)
    private String registrationHash;

    private Date registrationExpiration;
    
    @Indexed(unique = true)
    private String uid;
    
    private Gender gender;
    
    private String title; // personalTitle or academicTitle
    
    private String firstName;
    
    private String lastName;
    
    private String dateOfBirth; // format yyyy-MM-dd
    
    private String preferredLocale = Locale.getDefault().toString();
    
    private String preferredTimeZoneID = TimeZone.getDefault().getID();
    
    @Indexed(unique = true)
    private String email;
    
    private String password;
    
    private String sambaLmPassword;
    
    private String sambaNtPassword;
    
}
