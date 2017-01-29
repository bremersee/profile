/*
 * Copyright 2017 the original author or authors.
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

package org.bremersee.profile.domain.ldap;

import org.apache.commons.lang3.StringUtils;
import org.bremersee.common.exception.BadRequestException;
import org.bremersee.profile.domain.ldap.dao.AbstractLdapProperties;
import org.bremersee.utils.CodingUtils;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Christian Bremer
 */
public abstract class LdapEntryUtils {

    private static final Logger LOG = LoggerFactory.getLogger(LdapEntryUtils.class);

    private LdapEntryUtils() {
        super();
    }

    public static String createDn(final String rdnValue, final AbstractLdapProperties properties) {
        BadRequestException.validateNotBlank(rdnValue, "Rdn value must be present.");
        BadRequestException.validateNotNull(properties, "Ldap properties are required.");
        BadRequestException.validateNotBlank(properties.getRdn(), "Rdn name is required.");
        StringBuilder sb = new StringBuilder();
        sb.append(properties.getRdn()).append('=').append(rdnValue);
        if (properties.getSearchRequest() != null
                && StringUtils.isNotBlank(properties.getSearchRequest().getBaseDn())) {
            sb.append(',').append(properties.getSearchRequest().getBaseDn());
        }
        return sb.toString();
    }

    @SuppressWarnings("unused")
    public static Long getLong(final LdapEntry entry, final String attribute, final Long nullValue) {
        String value = getString(entry, attribute, null);
        if (value == null) {
            return nullValue;
        }
        try {
            return Long.parseLong(value);
        } catch (Exception e) {
            LOG.error("Parsing number [" + value + "] failed. Returning default value [" + nullValue + "].", e);
            return nullValue;
        }
    }

    public static String getString(final LdapEntry entry, final String attribute, final String nullValue) {

        final LdapAttribute attr = entry.getAttribute(attribute);
        if (attr == null) {
            return nullValue;
        }

        String v;
        if (attr.isBinary()) {
            final byte[] b = attr.getBinaryValue();
            v = new String(b, Charset.forName("UTF-8"));
        } else {
            v = attr.getStringValue();
        }

        if (StringUtils.isNotBlank(v)) {
            return v;
        }
        return nullValue;
    }

    public static List<String> getStringList(final LdapEntry entry, final String attribute) {

        final LdapAttribute attr = entry.getAttribute(attribute);
        if (attr == null) {
            return new ArrayList<>();
        }
        if (attr.isBinary()) {
            final ValidBytesPredicate validBytesPredicate = new ValidBytesPredicate();
            final BytesToUtf8 bytesToUtf8 = new BytesToUtf8();
            return attr.getBinaryValues().stream().filter(validBytesPredicate).map(bytesToUtf8).collect(Collectors.toList());
        } else {
            return attr.getStringValues().stream().filter(StringUtils::isNotBlank).collect(Collectors.toList());
        }
    }

    private static class ValidBytesPredicate implements Predicate<byte[]> {

        @Override
        public boolean test(byte[] o) {
            return o != null && o.length > 0;
        }
    }

    private static class BytesToUtf8 implements Function<byte[], String> {

        @Override
        public String apply(byte[] bytes) {
            return CodingUtils.toStringSilently(bytes, StandardCharsets.UTF_8);
        }
    }

}
