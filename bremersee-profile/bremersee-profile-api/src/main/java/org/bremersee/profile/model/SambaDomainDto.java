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
@XmlRootElement(name = "sambaDomain")
@XmlType(name = "sambaDomainType", propOrder = {
        "sambaDomainName",
        "sambaSID",
        "sambaAlgorithmicRidBase",
        "sambaForceLogoff",
        "sambaLockoutDuration",
        "sambaLockoutObservationWindow",
        "sambaLockoutThreshold",
        "sambaLogonToChgPwd",
        "sambaMaxPwdAge",
        "sambaMinPwdAge",
        "sambaMinPwdLength",
        "sambaNextUserRid",
        "sambaPwdHistoryLength",
        "sambaRefuseMachinePwdChange",
        "uidNumber",
        "gidNumber"
})
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder(alphabetic = true)
@Data
//@formatter:on
public class SambaDomainDto implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String TYPE_ID = "SambaDomain";

    @XmlElement(name = "sambaDomainName", required = true)
    @JsonProperty(value = "sambaDomainName", required = true)
    @ApiModelProperty(name = "sambaDomainName", value = "The samba domain name.", required = true)
    private String sambaDomainName;

    @XmlElement(name = "sambaSID", required = true)
    @JsonProperty(value = "sambaSID", required = true)
    @ApiModelProperty(name = "sambaSID", value = "The samba SID.", required = true)
    private String sambaSID;

    @XmlElement(name = "sambaAlgorithmicRidBase")
    @JsonProperty(value = "sambaAlgorithmicRidBase")
    @ApiModelProperty(name = "sambaAlgorithmicRidBase", example = "1000")
    private Long sambaAlgorithmicRidBase = 1000L;

    @XmlElement(name = "sambaForceLogoff")
    @JsonProperty(value = "sambaForceLogoff")
    @ApiModelProperty(name = "sambaForceLogoff", example = "-1")
    private Integer sambaForceLogoff = -1;

    @XmlElement(name = "sambaLockoutDuration")
    @JsonProperty(value = "sambaLockoutDuration")
    @ApiModelProperty(name = "sambaLockoutDuration", example = "30")
    private Integer sambaLockoutDuration = 30;

    @XmlElement(name = "sambaLockoutObservationWindow")
    @JsonProperty(value = "sambaLockoutObservationWindow")
    @ApiModelProperty(name = "sambaLockoutObservationWindow", example = "30")
    private Integer sambaLockoutObservationWindow = 30;

    @XmlElement(name = "sambaLockoutThreshold")
    @JsonProperty(value = "sambaLockoutThreshold")
    @ApiModelProperty(name = "sambaLockoutThreshold", example = "0")
    private Integer sambaLockoutThreshold = 0;

    @XmlElement(name = "sambaLogonToChgPwd")
    @JsonProperty(value = "sambaLogonToChgPwd")
    @ApiModelProperty(name = "sambaLogonToChgPwd", example = "0")
    private Integer sambaLogonToChgPwd = 0;

    @XmlElement(name = "sambaMaxPwdAge")
    @JsonProperty(value = "sambaMaxPwdAge")
    @ApiModelProperty(name = "sambaMaxPwdAge", example = "-1")
    private Integer sambaMaxPwdAge = -1;

    @XmlElement(name = "sambaMinPwdAge")
    @JsonProperty(value = "sambaMinPwdAge")
    @ApiModelProperty(name = "sambaMinPwdAge", example = "0")
    private Integer sambaMinPwdAge = 0;

    @XmlElement(name = "sambaMinPwdLength")
    @JsonProperty(value = "sambaMinPwdLength")
    @ApiModelProperty(name = "sambaMinPwdLength", example = "5")
    private Integer sambaMinPwdLength = 0;

    @XmlElement(name = "sambaNextUserRid")
    @JsonProperty(value = "sambaNextUserRid")
    @ApiModelProperty(name = "sambaNextUserRid", example = "1000")
    private Long sambaNextUserRid = 1000L;

    @XmlElement(name = "sambaPwdHistoryLength")
    @JsonProperty(value = "sambaPwdHistoryLength")
    @ApiModelProperty(name = "sambaPwdHistoryLength", example = "0")
    private Integer sambaPwdHistoryLength = 0;

    @XmlElement(name = "sambaRefuseMachinePwdChange")
    @JsonProperty(value = "sambaRefuseMachinePwdChange")
    @ApiModelProperty(name = "sambaRefuseMachinePwdChange", example = "0")
    private Integer sambaRefuseMachinePwdChange = 0;

    /**
     * The next UID number. After it has been assigned, it must be incremented.
     */
    @XmlElement(name = "uidNumber")
    @JsonProperty(value = "uidNumber")
    @ApiModelProperty(name = "uidNumber")
    private Long uidNumber;

    /**
     * The next GID Number. After it has been assigned, it must be incremented.
     */
    @XmlElement(name = "gidNumber")
    @JsonProperty(value = "gidNumber")
    @ApiModelProperty(name = "gidNumber")
    private Long gidNumber;

}
