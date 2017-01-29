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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bremersee.common.model.AbstractBaseDto;

import javax.xml.bind.annotation.*;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Christian Bremer
 */
//@formatter:off
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "oAuth2Client")
@XmlType(name = "oAuth2ClientType", propOrder = {
        "clientId",
        "resourceIds",
        "scope",
        "authorizedGrantTypes",
        "registeredRedirectUri",
        "roles",
        "accessTokenValiditySeconds",
        "refreshTokenValiditySeconds",
        "autoApproveScopes",
        "additionalInformation"
})
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder(alphabetic = true)
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
//@formatter:on
public class OAuth2ClientDto extends AbstractBaseDto {

    private static final long serialVersionUID = 1L;

    public static final String TYPE_ID = "OAuth2Client";

    @XmlElement(name = "clientId", required = true)
    @ApiModelProperty(value = "The name of the client", required = true)
    private String clientId;

    @XmlElementWrapper(name = "resourceIds")
    @XmlElement(name = "resourceId")
    @JsonProperty(value = "resourceIds")
    private Set<String> resourceIds = new LinkedHashSet<>();

    @XmlElementWrapper(name = "scopes")
    @XmlElement(name = "scope")
    @JsonProperty(value = "scope")
    private Set<String> scope = new LinkedHashSet<>();

    @XmlElementWrapper(name = "authorizedGrantTypes")
    @XmlElement(name = "authorizedGrantType")
    @JsonProperty(value = "authorizedGrantTypes")
    private Set<String> authorizedGrantTypes = new LinkedHashSet<>();

    @XmlElementWrapper(name = "registeredRedirectUris")
    @XmlElement(name = "registeredRedirectUri")
    @JsonProperty(value = "registeredRedirectUri")
    private Set<String> registeredRedirectUri = new LinkedHashSet<>();

    @XmlElementWrapper(name = "roles")
    @XmlElement(name = "role")
    @JsonProperty(value = "roles")
    private Set<String> roles = new LinkedHashSet<>();

    @XmlElement(name = "accessTokenValiditySeconds")
    private Integer accessTokenValiditySeconds;

    @XmlElement(name = "refreshTokenValiditySeconds")
    private Integer refreshTokenValiditySeconds;

    @XmlElementWrapper(name = "autoApproveScopes")
    @XmlElement(name = "autoApproveScope")
    @JsonProperty(value = "autoApproveScopes")
    private Set<String> autoApproveScopes = new LinkedHashSet<>();

    @ApiModelProperty("Additional information")
    private Map<String, Object> additionalInformation = new LinkedHashMap<>(); // NOSONAR

}
