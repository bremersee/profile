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
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.*;
import java.io.Serializable;

/**
 * @author Christian Bremer
 */
//@formatter:off
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "role")
@XmlType(name = "roleType", propOrder = {
        "name",
        "description"
})
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder(alphabetic = true)
@Data
@NoArgsConstructor
@ApiModel(value = "Role", description = "A role of an user.")
@AllArgsConstructor
//@formatter:on
public class RoleDto implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String TYPE_ID = "Role";

    public static final String SYSTEM_ROLE_NAME = "ROLE_SYSTEM";

    public static final String OAUTH2_CLIENT_ROLE_NAME = "ROLE_OAUTH2_CLIENT";

    public static final String USER_ROLE_NAME = "ROLE_USER";

    public static final String ADMIN_ROLE_NAME = "ROLE_ADMIN";

    public static final String ACL_ADMIN_ROLE_NAME = "ROLE_ACL_ADMIN";

    public static final String READ_ALL_PROFILES_ROLE_NAME = "ROLE_READ_ALL_PROFILES";

    @XmlElement(name = "name", required = true)
    @JsonProperty(value = "name", required = true)
    @ApiModelProperty(name = "name", value = "The name of the role.", required = true)
    private String name;

    @XmlElement(name = "description")
    @JsonProperty(value = "description")
    @ApiModelProperty(name = "description", value = "The description of the role.")
    private String description;

    /**
     * Creates a role with the specified name.
     *
     * @param name the role name
     */
    public RoleDto(final String name) {
        this.name = name;
    }

    public static RoleDto[] getDefaultRoles() {
        return new RoleDto[]{
                new RoleDto(SYSTEM_ROLE_NAME, "Role of system users and services."),
                new RoleDto(OAUTH2_CLIENT_ROLE_NAME, "Role of OAuth2 Clients."),
                new RoleDto(USER_ROLE_NAME, "Default user role."),
                new RoleDto(ADMIN_ROLE_NAME, "Administrator role."),
                new RoleDto(ACL_ADMIN_ROLE_NAME, "ACL administrator role."),
                new RoleDto(READ_ALL_PROFILES_ROLE_NAME, "Read all profiles role.")
        };
    }

}
