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
import org.bremersee.common.exception.AlreadyExistsException;
import org.bremersee.common.exception.BadRequestException;
import org.bremersee.common.exception.NotFoundException;
import org.bremersee.common.security.core.context.RunAsCallbackWithoutResult;
import org.bremersee.pagebuilder.PageBuilderUtils;
import org.bremersee.pagebuilder.model.Page;
import org.bremersee.pagebuilder.model.PageRequest;
import org.bremersee.pagebuilder.model.PageRequestDto;
import org.bremersee.profile.domain.ldap.dao.UserGroupLdapDao;
import org.bremersee.profile.domain.ldap.entity.UserGroupLdap;
import org.bremersee.profile.domain.ldap.mapper.UserGroupLdapMapper;
import org.bremersee.profile.model.UserGroupDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Set;

/**
 * @author Christian Bremer
 */
@Service("userGroupService")
@EnableConfigurationProperties(UserGroupProperties.class)
public class UserGroupServiceImpl extends AbstractServiceImpl implements UserGroupService {

    private static final String GROUP_NAME_MUST_BE_PRESENT = "Group name must be present.";

    private final UserGroupLdapDao userGroupLdapDao;

    private final UserGroupLdapMapper userGroupLdapMapper;

    private final SambaDomainService sambaDomainService;

    private final UserGroupProperties userGroupProperties;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    public UserGroupServiceImpl(
            final UserGroupLdapDao userGroupLdapDao,
            final UserGroupLdapMapper userGroupLdapMapper,
            final SambaDomainService sambaDomainService,
            final UserGroupProperties userGroupProperties) {

        this.userGroupLdapDao = userGroupLdapDao;
        this.userGroupLdapMapper = userGroupLdapMapper;
        this.sambaDomainService = sambaDomainService;
        this.userGroupProperties = userGroupProperties;
    }

    @Override
    protected void doInit() {
        runAsSystem(new Initializer());
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SYSTEM','ROLE_READ_ALL_PROFILES')")
    @Override
    public Page<UserGroupDto> findAll(final PageRequest request) {
        final PageRequest pageRequest = request == null ? new PageRequestDto() : request;
        log.info("{}: Find all user groups with page request [{}] ...", getCurrentUserName(), pageRequest);
        Page<UserGroupLdap> entities = userGroupLdapDao.findAll(pageRequest);
        Page<UserGroupDto> dtos = PageBuilderUtils.createPage(entities, userGroupLdapMapper::mapToDto);
        log.info("{}: Find all user groups with page request [{}]: Returning page no. {} with {} entries.",
                getCurrentUserName(), pageRequest, dtos.getPageRequest().getPageNumber(), dtos.getEntries().size());
        return dtos;
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SYSTEM')")
    @Override
    public UserGroupDto create(final UserGroupDto userGroup) {

        log.info("{}: Creating user group [{}] ...", getCurrentUserName(), userGroup);

        BadRequestException.validateNotNull(userGroup, "User group must be present.");
        BadRequestException.validateNotBlank(userGroup.getName(), GROUP_NAME_MUST_BE_PRESENT);

        checkExistence(userGroup);

        // validate GID number
        if (userGroup.getGidNumber() == null) {
            userGroup.setGidNumber(sambaDomainService.getNextGidNumber());
        }

        // validate samba settings
        if (!userGroup.isSambaGroup()) {
            userGroup.setSambaDomainName(null);
            userGroup.setSambaSID(null);
            userGroup.setSambaGroupType(null);
        } else {
            if (userGroup.getSambaGroupType() == null) {
                userGroup.setSambaGroupType(userGroupProperties.getDefaultSambaGroupType());
            }
            if (StringUtils.isBlank(userGroup.getSambaSID())) {
                if (StringUtils.isBlank(userGroup.getSambaDomainName())) {
                    final String sambaSID = sambaDomainService.getDefaultSambaSID(userGroup.getGidNumber());
                    userGroup.setSambaSID(sambaSID);
                    userGroup.setSambaDomainName(sambaDomainService.getDefaultSambaDomainName());
                } else {
                    final String sambaSID = sambaDomainService.getSambaSID(
                            userGroup.getGidNumber(),
                            userGroup.getSambaDomainName());
                    userGroup.setSambaSID(sambaSID);
                }
            }

        }

        UserGroupLdap entity = new UserGroupLdap();
        entity.setName(userGroup.getName());
        userGroupLdapMapper.updateEntity(userGroup, entity);
        entity = userGroupLdapDao.save(entity);
        initAcl(entity);
        UserGroupDto dto = userGroupLdapMapper.mapToDto(entity);
        log.info("{}: UserGroup [{}] successfully created.", getCurrentUserName(), dto);
        return dto;
    }

    private void checkExistence(final UserGroupDto userGroup) {
        if (userGroupLdapDao.existsByName(userGroup.getName())) {
            AlreadyExistsException e = new AlreadyExistsException(
                    String.format("UserGroup with name [%s] already exists.", userGroup.getName()));
            if (log.isErrorEnabled()) {
                log.error(String.format("%s: Creating user group failed.", getCurrentUserName()), e); // NOSONAR
            }
            throw e;
        }
        if (userGroup.getGidNumber() != null && userGroupLdapDao.existsByGidNumber(userGroup.getGidNumber())) {
            AlreadyExistsException e = new AlreadyExistsException(
                    String.format("UserGroup with GID number [%s] already exists.", userGroup.getGidNumber()));
            if (log.isErrorEnabled()) {
                log.error(String.format("%s: Creating user group failed.", getCurrentUserName()), e);
            }
            throw e;
        }
        if (userGroup.getSambaSID() != null && userGroupLdapDao.existsBySambaSID(userGroup.getSambaSID())) {
            AlreadyExistsException e = new AlreadyExistsException(
                    String.format("UserGroup with Samba SID [%s] already exists.", userGroup.getSambaSID()));
            if (log.isErrorEnabled()) {
                log.error(String.format("%s: Creating user group failed.", getCurrentUserName()), e);
            }
            throw e;
        }

        if (userGroup.getSambaSID() != null) {
            checkNewSambaSID(userGroup.getSambaSID());
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SYSTEM')"
            + " or hasPermission(#userGroupName, 'UserGroup', 'write')")
    @Override
    public UserGroupDto update(final String userGroupName, final UserGroupDto userGroup) {
        BadRequestException.validateNotBlank(userGroupName, GROUP_NAME_MUST_BE_PRESENT);
        BadRequestException.validateNotNull(userGroup, "User group must not be null.");

        // Renaming of a group is not supported.
        userGroup.setName(userGroupName);

        log.info("{}: Updating user group [{}] ...", getCurrentUserName(), userGroup);
        UserGroupLdap entity = userGroupLdapDao.findByName(userGroupName);
        NotFoundException.validateNotNull(entity, String.format("User group with name [%s] was not found.", userGroup));

        // validate GID number
        if (userGroup.getGidNumber() == null) {
            userGroup.setGidNumber(entity.getGidNumber());
        } else if (!userGroup.getGidNumber().equals(entity.getGidNumber())
                && userGroupLdapDao.existsByGidNumber(userGroup.getGidNumber())) {
            AlreadyExistsException aee = new AlreadyExistsException();
            log.error("");
            throw aee;
        }

        checkSambaGroup(userGroup, entity);

        userGroupLdapMapper.updateEntity(userGroup, entity);
        entity = userGroupLdapDao.save(entity);
        UserGroupDto dto = userGroupLdapMapper.mapToDto(entity);
        log.info("{}: UserGroup [{}] successfully updated.", getCurrentUserName(), dto);
        return dto;
    }

    private void checkSambaGroup(final UserGroupDto userGroup, final UserGroupLdap entity) {
        if (!userGroup.isSambaGroup()) {
            userGroup.setSambaSID(null);
            userGroup.setSambaDomainName(null);
            userGroup.setSambaGroupType(null);
        } else {
            checkSambaGroupValues(userGroup, entity);
        }
    }

    private void checkSambaGroupValues(final UserGroupDto userGroup, final UserGroupLdap entity) {
        if (userGroup.getSambaGroupType() == null) {
            final Integer gid;
            if (entity.getSambaGroupType() != null) {
                gid = entity.getSambaGroupType();
            } else {
                gid = userGroupProperties.getDefaultSambaGroupType();
            }
            userGroup.setSambaGroupType(gid);
        }
        if (StringUtils.isBlank(userGroup.getSambaSID())) {
            final String sid;
            if (StringUtils.isNotBlank(entity.getSambaSID())) {
                sid = entity.getSambaSID();
            } else if (StringUtils.isBlank(userGroup.getSambaDomainName())) {
                sid = sambaDomainService.getDefaultSambaSID(userGroup.getGidNumber());
                userGroup.setSambaDomainName(sambaDomainService.getDefaultSambaDomainName());
            } else {
                sid = sambaDomainService.getSambaSID(userGroup.getGidNumber(), userGroup.getSambaDomainName());
            }
            userGroup.setSambaSID(sid);
        } else if (!userGroup.getSambaSID().equals(entity.getSambaSID())) {
            checkNewSambaSID(userGroup.getSambaSID());
        }
    }

    private void checkNewSambaSID(final String sambaSID) {
        final int index = sambaSID.lastIndexOf('-');
        BadRequestException.validateTrue(index > 0,
                String.format("Illegal Samba SID [%s].", sambaSID));
        final String sid = sambaSID.substring(0, index);
        BadRequestException.validateTrue(sambaDomainService.existsBySambaSID(sid),
                String.format("There's no samba domain with SID [%s]", sid));
    }

    @PreAuthorize("isAuthenticated()")
    @PostAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SYSTEM','ROLE_READ_ALL_PROFILES')"
            + " or hasPermission(#returnObject.name, 'UserGroup', 'read')")
    @Override
    public UserGroupDto findByIdentifier(final String identifier) {
        log.info("{}: Find user group by identifier [{}] ...", getCurrentUserName(), identifier);
        BadRequestException.validateNotBlank(identifier, "User group identifier must be present.");
        UserGroupLdap entity = userGroupLdapDao.findByName(identifier);
        if (entity == null) {
            entity = userGroupLdapDao.findBySambaSID(identifier);
            if (entity == null) {
                try {
                    entity = userGroupLdapDao.findByGidNumber(Long.parseLong(identifier));
                } catch (NumberFormatException e) { // NOSONAR
                    entity = null;
                }
            }
        }
        NotFoundException.validateNotNull(entity,
                String.format("User group with identifier [%s] was not found.", identifier));
        UserGroupDto dto = userGroupLdapMapper.mapToDto(entity);
        log.info("{}: Find user group by identifier [{}]: DONE!", getCurrentUserName(), identifier);
        return dto;
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SYSTEM','ROLE_READ_ALL_PROFILES')"
            + " or hasPermission(#userGroupName, 'UserGroup', 'read')")
    @Override
    public UserGroupDto findByName(final String userGroupName) {
        log.info("{}: Find user group by name [{}] ...", getCurrentUserName(), userGroupName);
        BadRequestException.validateNotBlank(userGroupName, GROUP_NAME_MUST_BE_PRESENT);
        UserGroupLdap entity = userGroupLdapDao.findByName(userGroupName);
        NotFoundException.validateNotNull(entity, "User group with name [" + userGroupName + "] was not found.");
        UserGroupDto dto = userGroupLdapMapper.mapToDto(entity);
        log.info("{}: Find user group by name [{}]: DONE!", getCurrentUserName(), userGroupName);
        return dto;
    }

    @PreAuthorize("isAuthenticated()")
    @PostAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SYSTEM','ROLE_READ_ALL_PROFILES')"
            + " or hasPermission(#returnObject.name, 'UserGroup', 'read')")
    @Override
    public UserGroupDto findByGidNumber(final long gidNumber) {
        log.info("{}: Find user group by GID number [{}] ...", getCurrentUserName(), gidNumber);
        UserGroupLdap entity = userGroupLdapDao.findByGidNumber(gidNumber);
        NotFoundException.validateNotNull(entity,
                String.format("User group with GID number [%s] was not found.", gidNumber));
        UserGroupDto dto = userGroupLdapMapper.mapToDto(entity);
        log.info("{}: Find user group by GID number [{}]: DONE!", getCurrentUserName(), gidNumber);
        return dto;
    }

    @PreAuthorize("isAuthenticated()")
    @PostAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SYSTEM','ROLE_READ_ALL_PROFILES')"
            + " or hasPermission(#returnObject.name, 'UserGroup', 'read')")
    @Override
    public UserGroupDto findBySambaSID(final String sambaSID) {
        log.info("{}: Find user group by Samba SIS [{}] ...", getCurrentUserName(), sambaSID);
        BadRequestException.validateNotBlank(sambaSID, "Samba SID must be present.");
        UserGroupLdap entity = userGroupLdapDao.findBySambaSID(sambaSID);
        NotFoundException.validateNotNull(entity,
                String.format("User group with Samba SID [%s] was not found.", sambaSID));
        UserGroupDto dto = userGroupLdapMapper.mapToDto(entity);
        log.info("{}: Find user group by Samba SID [{}]: DONE!", getCurrentUserName(), sambaSID);
        return dto;
    }

    @PreAuthorize("isAuthenticated()")
    @Override
    public boolean existsByIdentifier(final String identifier) {
        log.info("{}: User group with identifier [{}] exists? ...", getCurrentUserName(), identifier);
        BadRequestException.validateNotBlank(identifier, "User group identifier must be present.");
        Long gidNumber;
        try {
            gidNumber = Long.parseLong(identifier);
        } catch (NumberFormatException ignored) {
            gidNumber = null;
        }
        final boolean result = userGroupLdapDao.existsByName(identifier)
                || userGroupLdapDao.existsBySambaSID(identifier)
                || (gidNumber != null && userGroupLdapDao.existsByGidNumber(gidNumber));
        log.info("{}: User group with identifier [{}] exists? {}", getCurrentUserName(), identifier, result);
        return result;
    }

    @PreAuthorize("isAuthenticated()")
    @Override
    public boolean existsByName(final String userGroupName) {
        log.info("{}: User group with name [{}] exists? ...", getCurrentUserName(), userGroupName);
        BadRequestException.validateNotBlank(userGroupName, GROUP_NAME_MUST_BE_PRESENT);
        final boolean result = userGroupLdapDao.existsByName(userGroupName);
        log.info("{}: User group with name [{}] exists? {}", getCurrentUserName(), userGroupName, result);
        return result;
    }

    @PreAuthorize("isAuthenticated()")
    @Override
    public boolean existsByGidNumber(final long gidNumber) {
        log.info("{}: User group with GID number [{}] exists? ...", getCurrentUserName(), gidNumber);
        final boolean result = userGroupLdapDao.existsByGidNumber(gidNumber);
        log.info("{}: User group with GID number [{}] exists? {}", getCurrentUserName(), gidNumber, result);
        return result;
    }

    @PreAuthorize("isAuthenticated()")
    @Override
    public boolean existsBySambaSID(final String sambaSID) {
        log.info("{}: User group with Samba SID [{}] exists? ...", getCurrentUserName(), sambaSID);
        BadRequestException.validateNotBlank(sambaSID, "Samba SID must be present.");
        final boolean result = userGroupLdapDao.existsBySambaSID(sambaSID);
        log.info("{}: User group with Samba SID [{}] exists? {}", getCurrentUserName(), sambaSID, result);
        return result;
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SYSTEM')"
            + " or hasPermission(#userGroupName, 'UserGroup', 'delete')")
    @Override
    public void deleteByName(final String userGroupName) {
        log.info("{}: Deleting user group with name [{}] ...", getCurrentUserName(), userGroupName);
        BadRequestException.validateNotBlank(userGroupName, GROUP_NAME_MUST_BE_PRESENT);
        userGroupLdapDao.deleteByName(userGroupName);
        deleteAcls(new ObjectIdentityImpl(UserGroupDto.TYPE_ID, userGroupName), true);
        log.info("{}: Deleting user group with name [{}]: DONE!", getCurrentUserName(), userGroupName);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SYSTEM','ROLE_READ_ALL_PROFILES')"
            + " or #member == authentication.name")
    @Override
    public Set<String> findUserGroupNamesByMember(final String member) {
        log.info("{}: Find user group names by member [{}] ...", getCurrentUserName(), member);
        BadRequestException.validateNotBlank(member, "Member must be present.");
        final Set<String> userGroupNames = userGroupLdapDao.findUserGroupNamesByMember(member);
        log.info("{}: Find user group names by member [{}]: {} found!",
                getCurrentUserName(), member, userGroupNames.size());
        return userGroupNames;
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SYSTEM','ROLE_READ_ALL_PROFILES')"
            + " or #member == authentication.name")
    @Override
    public boolean isGroupMember(final String member, final String userGroupName) {
        log.info("{}: Is user [{}] member of user group [{}]? ...", getCurrentUserName(), member, userGroupName);
        BadRequestException.validateNotBlank(userGroupName, GROUP_NAME_MUST_BE_PRESENT);
        final boolean result = userGroupLdapDao.hasUserGroup(member, userGroupName);
        log.info("{}: Is user [{}] member of user group [{}]? {}", getCurrentUserName(), member, userGroupName, result);
        return result;
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SYSTEM','ROLE_READ_ALL_PROFILES')"
            + " or hasPermission(#userGroupName, 'UserGroup', 'read')"
            + " or hasPermission(#userGroupName, 'UserGroup', 'create')")
    @Override
    public Set<String> getMembers(final String userGroupName) {
        log.info("{}: Getting members of user group [{}] ...", getCurrentUserName(), userGroupName);
        BadRequestException.validateNotBlank(userGroupName, GROUP_NAME_MUST_BE_PRESENT);
        Set<String> members = userGroupLdapDao.getMembers(userGroupName);
        log.info("{}: Getting members of user group [{}]: {} found!",
                getCurrentUserName(), userGroupName, members.size());
        return members;
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SYSTEM')"
            + " or hasPermission(#userGroupName, 'UserGroup', 'create')")
    @Override
    public void addMembers(final String userGroupName, final Collection<String> members) {
        if (log.isDebugEnabled()) {
            log.debug("{}: Adding members to user group [{}] ... members = {}",
                    getCurrentUserName(), userGroupName, members);
        } else {
            log.info("{}: Adding members to user group [{}] ...", getCurrentUserName(), userGroupName);
        }
        BadRequestException.validateNotBlank(userGroupName, GROUP_NAME_MUST_BE_PRESENT);
        userGroupLdapDao.addMembers(userGroupName, members);
        log.info("{}: Adding members to user group [{}]: DONE!", getCurrentUserName(), userGroupName);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SYSTEM')"
            + " or hasPermission(#userGroupName, 'UserGroup', 'create')")
    @Override
    public void removeMembers(final String userGroupName, final Collection<String> members) {
        if (log.isDebugEnabled()) {
            log.debug("{}: Removing members from user group [{}] ... members = {}",
                    getCurrentUserName(), userGroupName, members);
        } else {
            log.info("{}: Removing members from user group [{}] ...", getCurrentUserName(), userGroupName);
        }
        BadRequestException.validateNotBlank(userGroupName, GROUP_NAME_MUST_BE_PRESENT);
        userGroupLdapDao.removeMembers(userGroupName, members);
        log.info("{}: Removing members from user group [{}]: DONE!", getCurrentUserName(), userGroupName);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SYSTEM')"
            + " or hasPermission(#userGroupName, 'UserGroup', 'create')")
    @Override
    public void updateMembers(final String userGroupName, final Collection<String> members) {
        if (log.isDebugEnabled()) {
            log.debug("{}: Updating members of user group [{}] ... members = {}",
                    getCurrentUserName(), userGroupName, members);
        } else {
            log.info("{}: Updating members of user group [{}] ...", getCurrentUserName(), userGroupName);
        }
        BadRequestException.validateNotBlank(userGroupName, GROUP_NAME_MUST_BE_PRESENT);
        userGroupLdapDao.updateMembers(userGroupName, members);
        log.info("{}: Updating members of user group [{}]: DONE!", getCurrentUserName(), userGroupName);
    }

    private class Initializer extends RunAsCallbackWithoutResult {

        @Override
        public void run() {
            Page<UserGroupDto> domains = findAll(new PageRequestDto());
            for (final UserGroupDto entity : domains.getEntries()) {
                ObjectIdentity objectIdentity = getObjectIdentityRetrievalStrategy().getObjectIdentity(entity);
                try {
                    getAclService().readAclById(objectIdentity);
                } catch (org.springframework.security.acls.model.NotFoundException e) { // NOSONAR
                    log.info("No ACL found for user group [{}] - creating default ACL.", entity.getName());
                    initAcl(objectIdentity);
                }
            }
        }
    }

}
