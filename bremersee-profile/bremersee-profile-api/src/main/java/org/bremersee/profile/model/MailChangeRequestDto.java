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
import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bremersee.common.model.AbstractBaseDto;

import javax.xml.bind.annotation.*;
import java.util.Date;

/**
 * @author Christian Bremer
 */
//@formatter:off
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "mailChangeRequest")
@XmlType(name = "mailChangeRequestType", propOrder = {
        "changeHash",
        "changeExpiration",
        "uid",
        "newEmail"
})
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder(alphabetic = true)
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
//@formatter:on
public class MailChangeRequestDto extends AbstractBaseDto {

    private static final long serialVersionUID = 1L;

    @XmlElement(name = "changeHash")
    @ApiModelProperty
    private String changeHash;

    @XmlElement(name = "changeExpiration")
    @ApiModelProperty
    private Date changeExpiration;

    @XmlElement(name = "uid", required = true)
    @ApiModelProperty(required = true)
    private String uid;

    @XmlElement(name = "newEmail", required = true)
    @ApiModelProperty(required = true)
    private String newEmail;

}
