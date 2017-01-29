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
import org.bremersee.profile.model.RoleDto;

import java.util.Collection;
import java.util.Set;

/**
 * @author Christian Bremer
 */
public interface RoleService {

    RoleDto createCustomRole(String description, String user);

    Page<RoleDto> findCustomRoles(PageRequest pageRequest, String user);

    void deleteCustomRoles(String user);


    Page<RoleDto> findAll(PageRequest pageRequest);

    RoleDto create(RoleDto role);

    RoleDto update(String roleName, RoleDto role);

    RoleDto findByName(String roleName);

    boolean existsByName(String roleName);

    void deleteByName(String roleName);


    Set<String> findRoleNamesByMember(String member);

    Set<String> findRoleNamesByMemberAndRoleNamePrefix(String member, String roleNamePrefix);

    boolean hasRole(String member, String roleName);


    Set<String> getMembers(String roleName);

    void addMembers(String roleName, Collection<String> members);

    void removeMembers(String roleName, Collection<String> members);

    void updateMembers(String roleName, Collection<String> members);

}
