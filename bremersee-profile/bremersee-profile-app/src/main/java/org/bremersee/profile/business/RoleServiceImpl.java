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
import org.bremersee.common.business.RoleNameService;
import org.bremersee.common.exception.AlreadyExistsException;
import org.bremersee.common.exception.BadRequestException;
import org.bremersee.common.exception.NotFoundException;
import org.bremersee.common.security.core.context.RunAsCallbackWithoutResult;
import org.bremersee.pagebuilder.PageBuilderUtils;
import org.bremersee.pagebuilder.model.Page;
import org.bremersee.pagebuilder.model.PageRequest;
import org.bremersee.pagebuilder.model.PageRequestDto;
import org.bremersee.profile.domain.ldap.dao.OAuth2ClientLdapDao;
import org.bremersee.profile.domain.ldap.dao.RoleLdapDao;
import org.bremersee.profile.domain.ldap.dao.UserProfileLdapDao;
import org.bremersee.profile.domain.ldap.entity.RoleLdap;
import org.bremersee.profile.domain.ldap.mapper.RoleLdapMapper;
import org.bremersee.profile.model.RoleDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Set;

/**
 * @author Christian Bremer
 */
@Service("roleService")
@EnableConfigurationProperties(RoleProperties.class)
public class RoleServiceImpl extends AbstractServiceImpl implements RoleService {

    private static final String ROLE_NAME_MUST_BE_PRESENT = "Role name must be present.";

    private final RoleNameService roleNameService;

    private final RoleLdapDao roleLdapDao;

    private final RoleLdapMapper roleLdapMapper;

    private UserProfileLdapDao userProfileLdapDao;

    private OAuth2ClientLdapDao oAuth2ClientLdapDao;

    private RoleProperties roleProperties = new RoleProperties();

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    public RoleServiceImpl(
            RoleNameService roleNameService,
            RoleLdapDao roleLdapDao,
            RoleLdapMapper roleLdapMapper,
            UserProfileLdapDao userProfileLdapDao,
            OAuth2ClientLdapDao oAuth2ClientLdapDao) {

        this.roleNameService = roleNameService;
        this.roleLdapDao = roleLdapDao;
        this.roleLdapMapper = roleLdapMapper;
        this.userProfileLdapDao = userProfileLdapDao;
        this.oAuth2ClientLdapDao = oAuth2ClientLdapDao;
    }

    @Autowired(required = false)
    public void setRoleProperties(RoleProperties roleProperties) {
        this.roleProperties = roleProperties;
    }

    @Override
    protected void doInit() {
        runAsSystemWithoutResult(new Initializer());
    }

    private boolean userExists(final String userName) {
        return userProfileLdapDao.existsByUserName(userName)
                || oAuth2ClientLdapDao.exists(userName);
    }

    private String generateCustomRoleName(final String userName) {
        String roleName = roleNameService.generateCustomRoleName(userName);
        while (roleLdapDao.existsByName(roleName)) {
            roleName = roleNameService.generateCustomRoleName(userName);
        }
        return roleName;
    }

    @PreAuthorize("isAuthenticated()")
    @Override
    public RoleDto createCustomRole(final String description, final String user) {
        final String userName;
        if (isCurrentUserAdminOrSystem() && StringUtils.isNotBlank(user)) {
            userName = user;
        } else {
            userName = getCurrentUserName();
        }
        log.info("{}: Creating custom role for user [{}] ...", getCurrentUserName(), userName);
        if (!userName.equals(getCurrentUserName())) {
            BadRequestException.validateTrue(userExists(userName),
                    String.format("User [%s] doesn't exist.", userName));
        }
        RoleDto customRole = runAsSystem(() -> {
            final String descr = StringUtils.isBlank(description) ?
                    String.format("Custom role of user [%s].", userName) : description;
            String roleName = generateCustomRoleName(userName);
            while (roleLdapDao.existsByName(roleName)) {
                roleName = generateCustomRoleName(userName);
            }
            RoleDto role = create(new RoleDto(roleName, descr));
            MutableAcl acl = (MutableAcl) getAclService().readAclById(getObjectIdentityRetrievalStrategy()
                    .getObjectIdentity(role));
            PrincipalSid ownerSid = new PrincipalSid(userName);
            acl.insertAce(acl.getEntries().size(), BasePermission.ADMINISTRATION, ownerSid, true);
            acl.insertAce(acl.getEntries().size(), BasePermission.CREATE, ownerSid, true);
            acl.insertAce(acl.getEntries().size(), BasePermission.DELETE, ownerSid, true);
            acl.insertAce(acl.getEntries().size(), BasePermission.READ, ownerSid, true);
            acl.insertAce(acl.getEntries().size(), BasePermission.WRITE, ownerSid, true);
            getAclService().updateAcl(acl);
            return role;
        });
        log.info("{}: Custom role [{}] successfully created.", getCurrentUserName(), customRole);
        return customRole;
    }

    @PreAuthorize("isAuthenticated()")
    @Override
    public Page<RoleDto> findCustomRoles(final PageRequest pageRequest, final String user) {
        final String userName;
        if (isCurrentUserAdminOrSystem() && StringUtils.isNotBlank(user)) {
            userName = user;
        } else {
            userName = getCurrentUserName();
        }
        log.info("{}: Getting custom roles of user [{}] ...", getCurrentUserName(), userName);
        final String roleNamePrefix = roleNameService.createCustomRoleNamePrefix(userName);
        Page<RoleLdap> entities = roleLdapDao.findByNameStartsWith(roleNamePrefix, pageRequest);
        Page<RoleDto> page = PageBuilderUtils.createPage(entities, roleLdapMapper::mapToDto);
        log.info("{}: Find custom roles of user [{}] with page request [{}]: Returning page no. {} with {} entries.",
                getCurrentUserName(), userName, pageRequest, page.getPageRequest(), page.getEntries().size());
        return page;
    }

    @PreAuthorize("isAuthenticated()")
    @Override
    public void deleteCustomRoles(final String user) {
        final String userName;
        if (isCurrentUserAdminOrSystem() && StringUtils.isNotBlank(user)) {
            userName = user;
        } else {
            userName = getCurrentUserName();
        }
        log.info("{}: Deleting custom roles of user [{}] ...", getCurrentUserName(), userName);
        final String roleNamePrefix = roleNameService.createCustomRoleNamePrefix(userName);
        final Set<String> deletedRoleNames = roleLdapDao.deleteByNameStartsWith(roleNamePrefix);
        deletedRoleNames.forEach(this::doPostDeleteRole);
        log.info("{}: Custom roles of user [{}] successfully deleted ({} role(s) was/were deleted).",
                getCurrentUserName(), userName, deletedRoleNames.size());
    }


    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SYSTEM','ROLE_READ_ALL_PROFILES')")
    @Override
    public Page<RoleDto> findAll(final PageRequest request) {
        final PageRequest pageRequest = request == null ? new PageRequestDto() : request;
        log.info("{}: Find all roles with page request [{}] ...", getCurrentUserName(), pageRequest);
        Page<RoleLdap> entities = roleLdapDao.findAll(pageRequest);
        Page<RoleDto> page = PageBuilderUtils.createPage(entities, roleLdapMapper::mapToDto);
        log.info("{}: Find all roles with page request [{}]: Returning page no. {} with {} entries.",
                getCurrentUserName(), pageRequest, page.getPageRequest().getPageNumber(), page.getEntries().size());
        return page;
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SYSTEM')")
    @Override
    public RoleDto create(final RoleDto role) {
        log.info("{}: Creating role [{}] ...", getCurrentUserName(), role);
        BadRequestException.validateNotNull(role, "Role must not be null.");
        BadRequestException.validateNotBlank(role.getName(), ROLE_NAME_MUST_BE_PRESENT);
        if (roleLdapDao.existsByName(role.getName())) {
            AlreadyExistsException e = new AlreadyExistsException("Role with name [" + role.getName() // NOSONAR
                    + "] already exists.");
            if (log.isErrorEnabled()) {
                log.error(String.format("%s: Creating role failed.", getCurrentUserName()), e);
            }
            throw e;
        }
        RoleLdap entity = new RoleLdap();
        entity.setName(role.getName());
        roleLdapMapper.updateEntity(role, entity);
        entity = roleLdapDao.save(entity);
        initAcl(entity);
        RoleDto dto = roleLdapMapper.mapToDto(entity);
        log.info("{}: Role [{}] successfully created.", getCurrentUserName(), dto);
        return dto;
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SYSTEM')"
            + " or hasPermission(#roleName, 'Role', 'write')")
    @Override
    public RoleDto update(final String roleName, final RoleDto role) {
        log.info("{}: Updating role with name [{}] ...", getCurrentUserName(), roleName);
        BadRequestException.validateNotBlank(roleName, "Role name must not present.");
        BadRequestException.validateNotNull(role, "Role must not be null.");
        role.setName(roleName);
        RoleLdap entity = roleLdapDao.findByName(roleName);
        NotFoundException.validateNotNull(entity, "Role with name [" + roleName + "] was not found.");
        roleLdapMapper.updateEntity(role, entity);
        entity = roleLdapDao.save(entity);
        RoleDto dto = roleLdapMapper.mapToDto(entity);
        log.info("{}: Role [{}] successfully updated.", getCurrentUserName(), dto);
        return dto;
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SYSTEM','ROLE_READ_ALL_PROFILES')"
            + " or hasPermission(#roleName, 'Role', 'read')")
    @Override
    public RoleDto findByName(final String roleName) {
        log.info("{}: Find role by name [{}] ...", getCurrentUserName(), roleName);
        BadRequestException.validateNotBlank(roleName, ROLE_NAME_MUST_BE_PRESENT);
        RoleLdap entity = roleLdapDao.findByName(roleName);
        NotFoundException.validateNotNull(entity, String.format("Role with name [%s] was not found.", roleName));
        RoleDto dto = roleLdapMapper.mapToDto(entity);
        log.info("{}: Find role by name [{}] ... DONE!", getCurrentUserName(), roleName);
        return dto;
    }

    @PreAuthorize("isAuthenticated()")
    @Override
    public boolean existsByName(final String roleName) {
        log.info("{}: Role with name [{}] exists? ...", getCurrentUserName(), roleName);
        BadRequestException.validateNotBlank(roleName, ROLE_NAME_MUST_BE_PRESENT);
        final boolean result = roleLdapDao.existsByName(roleName);
        log.info("{}: Role with name [{}] exists? ... {}", getCurrentUserName(), roleName, result);
        return result;
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SYSTEM')"
            + " or hasPermission(#roleName, 'Role', 'delete')")
    @Override
    public void deleteByName(final String roleName) {
        log.info("{}: Deleting role with name [{}] ...", getCurrentUserName(), roleName);
        BadRequestException.validateNotBlank(roleName, ROLE_NAME_MUST_BE_PRESENT);
        roleLdapDao.deleteByName(roleName);
        doPostDeleteRole(roleName);
        log.info("{}: Deleting role with name [{}] ... DONE!", getCurrentUserName(), roleName);
    }

    private void doPostDeleteRole(String roleName) {
        runAsSystem(() -> {
            deleteAcls(new ObjectIdentityImpl(RoleDto.TYPE_ID, roleName), true);
            return null;
        });
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SYSTEM','ROLE_READ_ALL_PROFILES')"
            + " or #member == authentication.name")
    @Override
    public Set<String> findRoleNamesByMember(final String member) {
        log.info("{}: Find role names by member [{}] ...", getCurrentUserName(), member);
        BadRequestException.validateNotBlank(member, "Member must be present.");
        final Set<String> roleNames = roleLdapDao.findRoleNamesByMember(member);
        log.info("{}: Find role names by member [{}]: {} found!", getCurrentUserName(), member, roleNames.size());
        return roleNames;
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SYSTEM','ROLE_READ_ALL_PROFILES')"
            + " or #member == authentication.name")
    @Override
    public Set<String> findRoleNamesByMemberAndRoleNamePrefix(final String member, final String roleNamePrefix) {
        log.info("{}: Find role names by member [{}] and role name prefix [{}] ...",
                getCurrentUserName(), member, roleNamePrefix);
        BadRequestException.validateNotBlank(member, "Member must be present.");
        BadRequestException.validateNotBlank(roleNamePrefix, "Role name prefix must be present.");
        final Set<String> roleNames = roleLdapDao.findRoleNamesByMember(member);
        log.info("{}: Find role names by member [{}] and role name prefix [{}]: {} found!",
                getCurrentUserName(), member, roleNamePrefix, roleNames.size());
        return roleNames;
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SYSTEM','ROLE_READ_ALL_PROFILES')"
            + " or #member == authentication.name")
    @Override
    public boolean hasRole(final String member, final String roleName) {
        log.info("{}: Is user [{}] member of role [{}]? ...", getCurrentUserName(), member, roleName);
        BadRequestException.validateNotBlank(roleName, ROLE_NAME_MUST_BE_PRESENT);
        final boolean result = roleLdapDao.hasRole(member, roleName);
        log.info("{}: Is user [{}] member of role [{}]? {}", getCurrentUserName(), member, roleName, result);
        return result;
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SYSTEM','ROLE_READ_ALL_PROFILES')"
            + " or hasPermission(#roleName, 'Role', 'read')"
            + " or hasPermission(#roleName, 'Role', 'create')")
    @Override
    public Set<String> getMembers(final String roleName) {
        log.info("{}: Getting members of role [{}] ...", getCurrentUserName(), roleName);
        BadRequestException.validateNotBlank(roleName, ROLE_NAME_MUST_BE_PRESENT);
        Set<String> members = roleLdapDao.getMembers(roleName);
        log.info("{}: Getting members of role [{}]: {} found!", getCurrentUserName(), roleName, members.size());
        return members;
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SYSTEM')"
            + " or hasPermission(#roleName, 'Role', 'create')")
    @Override
    public void addMembers(final String roleName, final Collection<String> members) {
        if (log.isDebugEnabled()) {
            log.debug("{}: Adding members to role [{}] ... members = {}", getCurrentUserName(), roleName, members);
        } else {
            log.info("{}: Adding members to role [{}] ...", getCurrentUserName(), roleName);
        }
        BadRequestException.validateNotBlank(roleName, ROLE_NAME_MUST_BE_PRESENT);
        roleLdapDao.addMembers(roleName, members);
        log.info("{}: Adding members to role [{}] ... DONE!", getCurrentUserName(), roleName);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SYSTEM')"
            + " or hasPermission(#roleName, 'Role', 'create')")
    @Override
    public void removeMembers(final String roleName, final Collection<String> members) {
        if (log.isDebugEnabled()) {
            log.debug("{}: Removing members from role [{}] ... members = {}", getCurrentUserName(), roleName, members);
        } else {
            log.info("{}: Removing members from role [{}] ...", getCurrentUserName(), roleName);
        }
        BadRequestException.validateNotBlank(roleName, ROLE_NAME_MUST_BE_PRESENT);
        roleLdapDao.removeMembers(roleName, members);
        log.info("{}: Removing members from role [{}] ... DONE!", getCurrentUserName(), roleName);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SYSTEM')"
            + " or hasPermission(#roleName, 'Role', 'create')")
    @Override
    public void updateMembers(final String roleName, final Collection<String> members) {
        if (log.isDebugEnabled()) {
            log.debug("{}: Updating members of role [{}] ... members = {}", getCurrentUserName(), roleName, members);
        } else {
            log.info("{}: Updating members of role [{}] ...", getCurrentUserName(), roleName);
        }
        BadRequestException.validateNotBlank(roleName, ROLE_NAME_MUST_BE_PRESENT);
        roleLdapDao.updateMembers(roleName, members);
        log.info("{}: Updating members of role [{}] ... DONE!", getCurrentUserName(), roleName);
    }

    private class Initializer extends RunAsCallbackWithoutResult {

        @Override
        public void run() {

            roleLdapDao.findAll(new PageRequestDto()).getEntries().forEach(entity -> {
                ObjectIdentity objectIdentity = getObjectIdentityRetrievalStrategy().getObjectIdentity(entity);
                try {
                    getAclService().readAclById(objectIdentity);
                } catch (org.springframework.security.acls.model.NotFoundException e) { // NOSONAR
                    log.info("No ACL found for role [" + entity.getName()
                            + "] - creating default ACL.");
                    initAcl(objectIdentity);
                }
            });
            for (RoleDto role : RoleDto.getDefaultRoles()) {
                if (!existsByName(role.getName())) {
                    create(role);
                }
            }
            for (RoleDto role : roleProperties.getInitRoles()) {
                if (!existsByName(role.getName())) {
                    create(role);
                }
            }
        }
    }
}
