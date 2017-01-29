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

package org.bremersee.profile.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModel;
import lombok.*;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Christian Bremer
 */
//@formatter:off
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "userProfile")
@XmlType(name = "userProfileType", propOrder = {
        "mobile",
        "homePhone",
        "homePostalAddress",
        "location",
        "state",
        "labeledURI"
})
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder(alphabetic = true)
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@ApiModel(
        value = "UserProfile",
        description = "A user profile contains data of an user.",
        parent = AbstractUserDto.class
)
//@formatter:on
public class UserProfileDto extends AbstractUserDto {

    private static final long serialVersionUID = 1L;

    public static final String TYPE_ID = "UserProfile";

    private String mobile;

    private String homePhone;

    private String homePostalAddress;

    private String location; //l

    private String state; //st

    private String labeledURI;

}
