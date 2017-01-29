package org.bremersee.profile.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.*;
import java.io.Serializable;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "userGroup")
@XmlType(name = "userGroupType", propOrder = {
        "name",
        "description",
        "gidNumber",
        "sambaGroup",
        "sambaGroupType",
        "sambaSID",
        "sambaDomainName"
})
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder(alphabetic = true)
@Data
@NoArgsConstructor
public class UserGroupDto implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String TYPE_ID = "UserGroup";

    @XmlElement(name = "name", required = true)
    @ApiModelProperty(name = "name", value = "The name of the group.", required = true)
    private String name;

    @XmlElement(name = "description")
    @ApiModelProperty(name = "description", value = "The description of the group.")
    private String description;

    @XmlElement(name = "gidNumber")
    @ApiModelProperty(name = "gidNumber", value = "The GID number.")
    private Long gidNumber;

    @XmlElement(name = "sambaGroup")
    @ApiModelProperty(name = "sambaGroup", value = "Is this group a samba group?")
    private boolean sambaGroup;

    @XmlElement(name = "sambaGroupType")
    @ApiModelProperty(name = "sambaGroupType", value = "The samba group type.")
    private Integer sambaGroupType;

    @XmlElement(name = "sambaSID")
    @ApiModelProperty(name = "sambaSID", value = "The samba SID.")
    private String sambaSID;

    @XmlElement(name = "sambaDomainName")
    @ApiModelProperty(name = "sambaDomainName", value = "The samba domain name.")
    private String sambaDomainName;

}
