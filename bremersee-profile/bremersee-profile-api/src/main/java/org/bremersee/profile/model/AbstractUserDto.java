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

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.bremersee.common.model.AbstractBaseDto;
import org.bremersee.common.model.Gender;
import org.bremersee.utils.LocaleUtils;
import org.bremersee.utils.TimeZoneUtils;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * @author Christian Bremer
 */
//@formatter:off
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "abstractUserType", propOrder = {
        "uid",
        "gender",
        "title",
        "firstName",
        "lastName",
        "dateOfBirthString",
        "preferredLocaleString",
        "preferredTimeZoneID",
        "email"
})
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder(alphabetic = true)
@JsonTypeInfo(use = Id.NAME, property = "type")
@JsonSubTypes({
        @Type(value = UserProfileDto.class, name = "UserProfile"),
        @Type(value = UserRegistrationDto.class, name = "UserRegistration"),
        @Type(value = UserRegistrationRequestDto.class, name = "UserRegistrationRequest"),
        @Type(value = UserProfileCreateRequestDto.class, name = "UserProfileCreateRequest")
})
@ApiModel(value = "AbstractUser",
        description = "Common user modell.",
        parent = AbstractBaseDto.class,
        subTypes = {
                UserProfileDto.class,
                UserRegistrationDto.class,
                UserRegistrationRequestDto.class},
        discriminator = "type")
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
//@formatter:on
public abstract class AbstractUserDto extends AbstractBaseDto {

    private static final long serialVersionUID = 1L;

    public static final String DATE_OF_BIRTH_PATTERN = "yyyy-MM-dd";

    protected static final SimpleDateFormat DATE_OF_BIRTH_SDF; // NOSONAR

    static {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_OF_BIRTH_PATTERN);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        DATE_OF_BIRTH_SDF = sdf;
    }

    @JsonIgnore
    private final transient Object preferredLocaleLock = new Object();

    @XmlElement(name = "uid", required = true)
    private String uid;

    @XmlElement(name = "gender")
    private Gender gender;

    @XmlElement(name = "title")
    private String title; // personalTitle or academicTitle

    @XmlElement(name = "firstName", required = true)
    private String firstName;

    @XmlElement(name = "lastName", required = true)
    private String lastName;

    @XmlElement(name = "dateOfBirth")
    private String dateOfBirthString; // format yyyy-MM-dd

    @XmlElement(name = "preferredLocale")
    private String preferredLocaleString = Locale.getDefault().toString();

    @XmlElement(name = "preferredTimeZoneID")
    private String preferredTimeZoneID = TimeZone.getDefault().getID();

    @XmlElement(name = "email")
    private String email;

    @ApiModelProperty(hidden = true)
    @JsonIgnore
    public Date getDateOfBirth() {
        if (StringUtils.isBlank(dateOfBirthString)) {
            return null;
        }
        try {
            return DATE_OF_BIRTH_SDF.parse(dateOfBirthString);
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @JsonIgnore
    public void setDateOfBirth(Date dateOfBirth) {
        if (dateOfBirth == null) {
            this.dateOfBirthString = null;
        } else {
            this.dateOfBirthString = DATE_OF_BIRTH_SDF.format(dateOfBirth);
        }
    }

    @ApiModelProperty(hidden = true)
    @JsonIgnore
    public Locale getPreferredLocale() {
        synchronized (preferredLocaleLock) {
            return LocaleUtils.fromString(preferredLocaleString, true);
        }
    }

    @JsonIgnore
    public void setPreferredLocale(Locale preferredLocale) {
        synchronized (preferredLocaleLock) {
            if (preferredLocale == null) {
                this.preferredLocaleString = null;
            } else {
                this.preferredLocaleString = preferredLocale.toString();
            }
        }
    }

    @ApiModelProperty(hidden = true)
    @JsonIgnore
    public String getPreferredLanguage() {
        synchronized (preferredLocaleLock) {
            return LocaleUtils.fromString(preferredLocaleString, false).getLanguage();
        }
    }

    @JsonIgnore
    public void setPreferredLanguage(String preferredLanguage) {
        synchronized (preferredLocaleLock) {
            Locale locale = LocaleUtils.fromString(preferredLocaleString, true);
            String language = LocaleUtils.fromString(preferredLanguage, false).getLanguage();
            String country = locale.getCountry();
            String variant = locale.getVariant();
            setPreferredLocale(language, country, variant);
        }
    }

    @ApiModelProperty(hidden = true)
    @JsonIgnore
    public String getPreferredCountry() {
        synchronized (preferredLocaleLock) {
            return LocaleUtils.fromString(preferredLocaleString, true).getCountry();
        }
    }

    @JsonIgnore
    public void setPreferredCountry(String preferredCountry) {
        synchronized (preferredLocaleLock) {
            Locale locale = LocaleUtils.fromString(preferredLocaleString, true);
            String language = locale.getLanguage();
            String country = LocaleUtils
                    .fromString(language + "_" + preferredCountry, true).getCountry();
            String variant = locale.getVariant();
            setPreferredLocale(language, country, variant);
        }
    }

    @JsonIgnore
    private void setPreferredLocale(String language, String country, String variant) {
        if (StringUtils.isNoneBlank(language, country, variant)) {
            preferredLocaleString = new Locale(language, country, variant).toString();
        } else if (StringUtils.isNoneBlank(language, country)) {
            preferredLocaleString = new Locale(language, country).toString();
        } else {
            preferredLocaleString = new Locale(language).toString();
        }
    }

    @ApiModelProperty(hidden = true)
    @JsonIgnore
    public TimeZone getPreferredTimeZone() {
        return TimeZone.getTimeZone(TimeZoneUtils.validateTimeZoneId(preferredTimeZoneID));
    }

    @JsonIgnore
    public void setPreferredTimeZone(TimeZone preferredTimeZone) {
        if (preferredTimeZone == null) {
            this.preferredTimeZoneID = null;
        } else {
            this.preferredTimeZoneID = preferredTimeZone.getID();
        }
    }

}
