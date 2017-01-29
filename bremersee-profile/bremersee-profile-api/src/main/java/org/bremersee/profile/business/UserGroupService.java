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
import org.bremersee.profile.model.UserGroupDto;

import java.util.Collection;
import java.util.Set;

/**
 * @author Christian Bremer
 */
public interface UserGroupService {

    Page<UserGroupDto> findAll(PageRequest pageRequest);

    UserGroupDto create(UserGroupDto userGroup);

    UserGroupDto update(String userGroupName, UserGroupDto userGroup);

    UserGroupDto findByIdentifier(String identifier);

    UserGroupDto findByName(String userGroupName);

    UserGroupDto findByGidNumber(long gidNumber);

    UserGroupDto findBySambaSID(String sambaSID);

    boolean existsByIdentifier(String identifier);

    boolean existsByName(String userGroupName);

    boolean existsByGidNumber(long gidNumber);

    boolean existsBySambaSID(String sambaSID);

    void deleteByName(String userGroupName);


    Set<String> findUserGroupNamesByMember(String member);

    boolean isGroupMember(String member, String userGroupName);


    Set<String> getMembers(String userGroupName);

    void addMembers(String userGroupName, Collection<String> members);

    void removeMembers(String userGroupName, Collection<String> members);

    void updateMembers(String userGroupName, Collection<String> members);

}
