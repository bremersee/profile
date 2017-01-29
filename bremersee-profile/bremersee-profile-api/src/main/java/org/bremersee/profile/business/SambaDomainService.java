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

package org.bremersee.profile.business;

import org.bremersee.pagebuilder.model.Page;
import org.bremersee.pagebuilder.model.PageRequest;
import org.bremersee.profile.model.SambaDomainDto;

/**
 * @author Christian Bremer
 *
 */
public interface SambaDomainService {

    String getDefaultSambaDomainName();

    String getDefaultSambaSID();

    SambaDomainDto getDefaultSambaDomain();

    long getNextUidNumber();

    long getNextGidNumber();

    String getDefaultSambaSID(long gidOrUidNumber);

    String getSambaSID(long gidOrUidNumber, String sambaDomainNameOrSambaSID);


    SambaDomainDto create(SambaDomainDto sambaDomain);
    
    SambaDomainDto update(String sambaDomainName, SambaDomainDto sambaDomain);
    
    Page<SambaDomainDto> findAll(PageRequest pageRequest);

    SambaDomainDto findBySambaDomainName(String sambaDomainName);

    SambaDomainDto findBySambaSID(String sambaSID);

    boolean existsBySambaDomainName(String sambaDomainName);

    boolean existsBySambaSID(String sambaSID);

    void deleteBySambaDomainName(String sambaDomainName);

}
