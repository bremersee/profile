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

package org.bremersee.profile.controller.rest;

import org.apache.commons.lang3.StringUtils;
import org.bremersee.comparator.ComparatorItemDeserializer;
import org.bremersee.comparator.ComparatorItemTransformerImpl;
import org.bremersee.pagebuilder.model.PageRequestDto;
import org.bremersee.profile.AbstractComponentImpl;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.StandardCharsets;

/**
 * @author Christian Bremer
 */
public abstract class AbstractRestControllerImpl extends AbstractComponentImpl {

    ComparatorItemDeserializer comparatorItemDeserializer = new ComparatorItemTransformerImpl();

    @Autowired(required = false)
    public void setComparatorItemDeserializer(final ComparatorItemDeserializer comparatorItemDeserializer) {
        if (comparatorItemDeserializer != null) {
            this.comparatorItemDeserializer = comparatorItemDeserializer;
        }
    }

    PageRequestDto createPageRequest(final Integer pageNumber, final Integer pageSize, final String comparatorItem,
                                     final String query) {

        PageRequestDto req = new PageRequestDto();
        if (pageNumber != null) {
            req.setPageNumber(pageNumber);
        }
        if (pageSize != null) {
            req.setPageSize(pageSize);
        }
        if (StringUtils.isNotBlank(comparatorItem)) {
            req.setComparatorItem(
                    comparatorItemDeserializer.fromString(comparatorItem, false, StandardCharsets.UTF_8.name()));
        }
        if (StringUtils.isNotBlank(query)) {
            req.setQuery(query);
        }
        return req;
    }

}
