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

package org.bremersee.profile.business;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.bremersee.common.business.RoleNameService;
import org.bremersee.common.exception.BadRequestException;
import org.bremersee.profile.model.RoleDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Christian Bremer
 */
@SuppressWarnings("unused")
@Service("friendsService")
public class FriendsServiceImpl extends AbstractServiceImpl implements FriendsService {

    private final RoleNameService roleNameService;

    private final RoleService roleService;

    @SuppressWarnings("SpringJavaAutowiredMembersInspection")
    @Autowired
    public FriendsServiceImpl(RoleNameService roleNameService, RoleService roleService) {
        this.roleNameService = roleNameService;
        this.roleService = roleService;
    }

    @Override
    protected void doInit() {
        Validate.notNull(roleNameService, "roleNameService must not be null");
        Validate.notNull(roleService, "roleService must not be null");
    }

    @Override
    public RoleDto getFriendsRole(final String user) {
        final String userName = StringUtils.isBlank(user) ? getCurrentUserName() : user;
        log.info("{}: Getting friends role of user [{}] ...", getCurrentUserName(), userName);
        final String roleName = roleNameService.createFriendsRoleName(userName);
        return roleService.findByName(roleName);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SYSTEM')")
    @Override
    public RoleDto createFriendsRole(final String user) {
        log.info("{}: Creating friends role of user [{}] ...", getCurrentUserName(), user);
        BadRequestException.validateNotBlank(user, "User name must be present.");
        final String roleName = roleNameService.createFriendsRoleName(user);
        if (roleService.existsByName(roleName)) {
            return roleService.findByName(roleName);
        }
        RoleDto role = new RoleDto();
        role.setName(roleName);
        role.setDescription("Friends role of " + user);
        role = roleService.create(role);
        PrincipalSid owner = new PrincipalSid(user);
        MutableAcl acl = (MutableAcl) getAclService().readAclById(
                getObjectIdentityRetrievalStrategy().getObjectIdentity(role));
        acl.setOwner(owner);
        acl.insertAce(acl.getEntries().size(), BasePermission.ADMINISTRATION, owner, true);
        acl.insertAce(acl.getEntries().size(), BasePermission.CREATE, owner, true);
        acl.insertAce(acl.getEntries().size(), BasePermission.READ, owner, true);
        getAclService().updateAcl(acl);
        return role;
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SYSTEM')")
    @Override
    public void deleteFriendsRole(final String user) {
        BadRequestException.validateNotBlank(user, "User name must be present.");
        log.info("{}: Deleting friends role or user [{}] ...", getCurrentUserName(), user);
        final String roleName = roleNameService.createFriendsRoleName(user);
        roleService.deleteByName(roleName);
    }

    @Override
    public void updateFriends(final String user, final Collection<String> friends) {
        final String userName = StringUtils.isBlank(user) ? getCurrentUserName() : user;
        log.info("{}: Updating friends of user [{}] ...", getCurrentUserName(), userName);
        final String roleName = roleNameService.createFriendsRoleName(userName);
        roleService.updateMembers(roleName, friends);
    }

    @Override
    public void addFriends(final String user, final Collection<String> friends) {
        final String userName = StringUtils.isBlank(user) ? getCurrentUserName() : user;
        log.info("{}: Adding friends to user [{}] ...", getCurrentUserName(), userName);
        final String roleName = roleNameService.createFriendsRoleName(userName);
        roleService.addMembers(roleName, friends);
    }

    @Override
    public void removeFriends(final String user, final Collection<String> friends) {
        final String userName = StringUtils.isBlank(user) ? getCurrentUserName() : user;
        log.info("{}: Removing friends from user [{}] ...", getCurrentUserName(), userName);
        final String roleName = roleNameService.createFriendsRoleName(userName);
        roleService.removeMembers(roleName, friends);
    }

    @Override
    public Set<String> getFriends(final String user) {
        final String userName = StringUtils.isBlank(user) ? getCurrentUserName() : user;
        log.info("{}: Finding friends of user [{}] ...", getCurrentUserName(), userName);
        final String roleName = roleNameService.createFriendsRoleName(userName);
        return roleService.getMembers(roleName);
    }

    @Override
    public Set<String> getUsersWhoHaveMeAsFriend(final String user) {
        final String userName = StringUtils.isBlank(user) ? getCurrentUserName() : user;
        log.info("{}: Finding users who have user [{}] as friend ...", getCurrentUserName(), userName);
        Set<String> roleNames = roleService.findRoleNamesByMemberAndRoleNamePrefix(user,
                RoleNameService.FRIENDS_ROLE_PREFIX);
        return roleNames.stream().map(roleNameService::getUserNameFromFriendsRoleName).collect(Collectors.toSet());
    }

}
