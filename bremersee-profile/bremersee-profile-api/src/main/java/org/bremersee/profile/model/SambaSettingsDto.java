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
 */
//@formatter:off
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "sambaSettings")
@XmlType(name = "sambaSettingsType", propOrder = {
        "sambaBadPasswordCount",
        "sambaBadPasswordTime",
        "sambaPwdLastSet",
        "sambaAcctFlags",
        "sambaDomainName",
        "sambaLogoffTime",
        "sambaLogonTime",
        "sambaMungedDial",
        "sambaPrimaryGroupSID",
        "sambaSID"
})
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder(alphabetic = true)
@Data
//@formatter:on
public class SambaSettingsDto implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String USER_PROFILE_ATTRIBUTE_NAME = "sambaSettings";

    @XmlAttribute(name = "sambaBadPasswordCount")
    @JsonProperty(value = "sambaBadPasswordCount")
    @ApiModelProperty(name = "sambaBadPasswordCount")
    private Integer sambaBadPasswordCount;

    @XmlAttribute(name = "sambaBadPasswordTime")
    @JsonProperty(value = "sambaBadPasswordTime")
    @ApiModelProperty(name = "sambaBadPasswordTime")
    private Integer sambaBadPasswordTime;

    @XmlAttribute(name = "sambaPwdLastSet")
    @JsonProperty(value = "sambaPwdLastSet")
    @ApiModelProperty(name = "sambaPwdLastSet")
    private Integer sambaPwdLastSet;


    @XmlAttribute(name = "sambaAcctFlags")
    @JsonProperty(value = "sambaAcctFlags")
    @ApiModelProperty(name = "sambaAcctFlags")
    private String sambaAcctFlags;

    @XmlAttribute(name = "sambaDomainName")
    @JsonProperty(value = "sambaDomainName")
    @ApiModelProperty(name = "sambaDomainName")
    private String sambaDomainName;

    @XmlAttribute(name = "sambaLogoffTime")
    @JsonProperty(value = "sambaLogoffTime")
    @ApiModelProperty(name = "sambaLogoffTime")
    private Integer sambaLogoffTime;

    @XmlAttribute(name = "sambaLogonTime")
    @JsonProperty(value = "sambaLogonTime")
    @ApiModelProperty(name = "sambaLogonTime")
    private Integer sambaLogonTime;

    @XmlAttribute(name = "sambaMungedDial")
    @JsonProperty(value = "sambaMungedDial")
    @ApiModelProperty(name = "sambaMungedDial")
    private String sambaMungedDial;

    @XmlAttribute(name = "sambaPrimaryGroupSID")
    @JsonProperty(value = "sambaPrimaryGroupSID")
    @ApiModelProperty(name = "sambaPrimaryGroupSID")
    private String sambaPrimaryGroupSID;

    @XmlAttribute(name = "sambaSID")
    @JsonProperty(value = "sambaSID")
    @ApiModelProperty(name = "sambaSID")
    private String sambaSID;

}
