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
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.xml.bind.annotation.*;
import java.io.Serializable;

/**
 * @author Christian Bremer
 *
 */
//@formatter:off
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "posixSettings")
@XmlType(name = "posixSettingsType", propOrder = {
        "gecos",
        "gidNumber",
        "homeDirectory",
        "loginShell",
        "uidNumber"
})
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder(alphabetic = true)
@Data
//@formatter:on
public class PosixSettingsDto implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String USER_PROFILE_ATTRIBUTE_NAME = "posixSettings";

    @XmlElement(name = "gecos", required = false)
    @JsonProperty(value = "gecos", required = false)
    @ApiModelProperty(name = "gecos", value = "The gecos of the user.", required = false)
    private String gecos;
    
    @XmlElement(name = "gidNumber", required = false)
    @JsonProperty(value = "gidNumber", required = false)
    @ApiModelProperty(name = "gidNumber", value = "The GID number of the user.", required = false)
    private Long gidNumber;
    
    @XmlElement(name = "homeDirectory", required = false)
    @JsonProperty(value = "homeDirectory", required = false)
    @ApiModelProperty(name = "homeDirectory", value = "The home directory of the user.", required = false)
    private String homeDirectory;
    
    @XmlElement(name = "loginShell", required = false)
    @JsonProperty(value = "loginShell", required = false)
    @ApiModelProperty(name = "loginShell", value = "The login shell of the user.", required = false)
    private String loginShell;
    
    @XmlElement(name = "uidNumber", required = false)
    @JsonProperty(value = "uidNumber", required = false)
    @ApiModelProperty(name = "uidNumber", value = "The UID number of the user.", required = false)
    private Long uidNumber;

}
