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
import org.apache.commons.lang3.StringUtils;

import javax.xml.bind.annotation.*;
import java.io.Serializable;

/**
 * @author Christian Bremer
 */
//@formatter:off
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "organisationSettings")
@XmlType(name = "organisationSettingsType", propOrder = {
        "departmentNumber",
        "employeeNumber",
        "employeeType",
        "facsimileTelephoneNumber",
        "manager",
        "organisation",
        "organisationUnit",
        "pager",
        "postalAddress",
        "roomNumber",
        "telephoneNumber"
})
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder(alphabetic = true)
@Data
//@formatter:off
public class OrganisationSettingsDto implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String USER_PROFILE_ATTRIBUTE_NAME = "organisationSettings";

    @XmlElement(required = false)
    @JsonProperty(required = false)
    @ApiModelProperty(value = "The department number.", required = false)
    private String departmentNumber; //Meine Abteilungs-Nr.

    @XmlElement(required = false)
    @JsonProperty(required = false)
    @ApiModelProperty(value = "The employee number.", required = false)
    private String employeeNumber; //Meine Angestellten-Nr.

    @XmlElement(required = false)
    @JsonProperty(required = false)
    @ApiModelProperty(value = "The employee type.", required = false)
    private String employeeType; //Meine Anstellungsart

    @XmlElement(required = false)
    @JsonProperty(required = false)
    @ApiModelProperty(value = "The facsimile telephone number.", required = false)
    private String facsimileTelephoneNumber;

    @XmlElement(name = "manager", required = false)
    @JsonProperty(value = "manager", required = false)
    @ApiModelProperty(name = "manager", value = "The user name of the manager.", required = false)
    private String manager;

    @XmlElement(name = "organisation", required = false)
    @JsonProperty(value = "organisation", required = false)
    @ApiModelProperty(name = "organisation", value = "The organisation of the user.", required = false)
    private String organisation; //o, Organisation

    @XmlElement(name = "organisationUnit", required = false)
    @JsonProperty(value = "organisationUnit", required = false)
    @ApiModelProperty(name = "organisationUnit", value = "The organisation unit of the user.", required = false)
    private String organisationUnit; //ou, Abteilung

    @XmlElement(name = "pager", required = false)
    @JsonProperty(value = "pager", required = false)
    @ApiModelProperty(name = "pager", value = "The pager number.", required = false)
    private String pager;

    @XmlElement(name = "postalAddress", required = false)
    @JsonProperty(value = "postalAddress", required = false)
    @ApiModelProperty(name = "postalAddress", value = "The postal address.", required = false)
    private String postalAddress;

    @XmlElement(name = "roomNumber", required = false)
    @JsonProperty(value = "roomNumber", required = false)
    @ApiModelProperty(name = "roomNumber", value = "The room number.", required = false)
    private String roomNumber;

    @XmlElement(name = "telephoneNumber", required = false)
    @JsonProperty(value = "telephoneNumber", required = false)
    @ApiModelProperty(name = "telephoneNumber", value = "The telephone number.", required = false)
    private String telephoneNumber;

    public boolean areAllValuesEmpty() {
        return StringUtils.isBlank(this.departmentNumber)
                && StringUtils.isBlank(this.employeeNumber)
                && StringUtils.isBlank(this.employeeType)
                && StringUtils.isBlank(this.facsimileTelephoneNumber)
                && StringUtils.isBlank(this.manager)
                && StringUtils.isBlank(this.organisation)
                && StringUtils.isBlank(this.organisationUnit)
                && StringUtils.isBlank(this.pager)
                && StringUtils.isBlank(this.postalAddress)
                && StringUtils.isBlank(this.roomNumber)
                && StringUtils.isBlank(this.telephoneNumber)
                ;
    }

}
