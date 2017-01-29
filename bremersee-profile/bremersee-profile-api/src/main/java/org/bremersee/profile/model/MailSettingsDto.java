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
import java.util.ArrayList;
import java.util.List;

/**
 * @author Christian Bremer
 */
//@formatter:off
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "mailSettings")
@XmlType(name = "mailSettingsType", propOrder = {
        "gosaMailAlternateAddresses",
        "gosaMailDeliveryMode",
        "gosaMailForwardingAddresses",
        "gosaMailQuota",
        "gosaMailServer",
        "gosaSpamMailbox",
        "gosaSpamSortLevel"
})
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder(alphabetic = true)
@Data
//@formatter:on
public class MailSettingsDto implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String USER_PROFILE_ATTRIBUTE_NAME = "mailSettings";

    @XmlElementWrapper(name = "gosaMailAlternateAddresses")
    @XmlElement(name = "gosaMailAlternateAddress")
    @JsonProperty(value = "gosaMailAlternateAddresses")
    @ApiModelProperty(name = "gosaMailAlternateAddresses", value = "Alternate mail addresses of the user.")
    private List<String> gosaMailAlternateAddresses = new ArrayList<>();

    @XmlElement(name = "gosaMailDeliveryMode")
    @ApiModelProperty(name = "gosaMailDeliveryMode", value = "The delivery mode.")
    private String gosaMailDeliveryMode;

    @XmlElementWrapper(name = "gosaMailForwardingAddresses")
    @XmlElement(name = "gosaMailForwardingAddress")
    @JsonProperty(value = "gosaMailForwardingAddresses")
    @ApiModelProperty(name = "gosaMailForwardingAddresses", value = "Forward mail addresses of the user.")
    private List<String> gosaMailForwardingAddresses = new ArrayList<>();

    @XmlElement(name = "gosaMailQuota")
    @ApiModelProperty(name = "gosaMailQuota", value = "The quota of the mail account.")
    private String gosaMailQuota;

    @XmlElement(name = "gosaMailServer")
    @ApiModelProperty(name = "gosaMailServer", value = "The mail server for the user.")
    private String gosaMailServer;

    @XmlElement(name = "gosaSpamMailbox")
    @ApiModelProperty(name = "gosaSpamMailbox", value = "The spam mailbox.")
    private String gosaSpamMailbox;

    @XmlElement(name = "gosaSpamSortLevel")
    @ApiModelProperty(name = "gosaSpamSortLevel", value = "The spam level.")
    private String gosaSpamSortLevel;

}
