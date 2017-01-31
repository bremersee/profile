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

import org.apache.commons.lang3.StringUtils;
import org.bremersee.common.business.RoleNameService;
import org.bremersee.common.exception.BadRequestException;
import org.bremersee.common.exception.ForbiddenException;
import org.bremersee.common.exception.NotFoundException;
import org.bremersee.common.security.core.context.RunAsCallbackWithoutResult;
import org.bremersee.common.security.crypto.password.PasswordEncoder;
import org.bremersee.pagebuilder.model.Page;
import org.bremersee.pagebuilder.model.PageRequest;
import org.bremersee.pagebuilder.model.PageRequestDto;
import org.bremersee.pagebuilder.spring.PageBuilderSpringUtils;
import org.bremersee.pagebuilder.spring.SpringPageRequest;
import org.bremersee.profile.domain.ldap.dao.UserProfileLdapDao;
import org.bremersee.profile.domain.ldap.entity.UserProfileLdap;
import org.bremersee.profile.domain.ldap.mapper.UserProfileLdapMapper;
import org.bremersee.profile.domain.mongodb.entity.UserProfileMongo;
import org.bremersee.profile.domain.mongodb.mapper.UserProfileMongoMapper;
import org.bremersee.profile.domain.mongodb.repository.UserProfileMongoRepository;
import org.bremersee.profile.model.*;
import org.bremersee.profile.validation.EmailValidator;
import org.bremersee.profile.validation.PasswordValidator;
import org.bremersee.profile.validation.UserNameValidator;
import org.bremersee.profile.validation.ValidatorConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Christian Bremer
 */
@Service("userProfileService")
@EnableConfigurationProperties(UserProfileProperties.class)
public class UserProfileServiceImpl extends AbstractUserProfileServiceImpl implements UserProfileService {

    private final RoleNameService roleNameService;

    private final RoleService roleService;

    private final FriendsService friendsService;

    private final UserGroupService userGroupService;

    private final PasswordEncoder passwordEncoder;

    private final PasswordValidator passwordValidator;

    private final UserNameValidator userNameValidator;

    private final EmailValidator emailValidator;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    public UserProfileServiceImpl( // NOSONAR
                                   final UserProfileLdapDao userProfileLdapDao,
                                   final UserProfileLdapMapper userProfileLdapMapper,
                                   final UserProfileMongoRepository userProfileMongoRepository,
                                   final UserProfileMongoMapper userProfileMongoMapper,

                                   final RoleNameService roleNameService,
                                   final RoleService roleService,
                                   final FriendsService friendsService,
                                   final UserGroupService userGroupService,
                                   final PasswordEncoder passwordEncoder,
                                   final PasswordValidator passwordValidator,
                                   final UserNameValidator userNameValidator,
                                   final EmailValidator emailValidator) {

        super(userProfileLdapDao, userProfileLdapMapper, userProfileMongoRepository, userProfileMongoMapper);
        this.roleNameService = roleNameService;
        this.roleService = roleService;
        this.friendsService = friendsService;
        this.userGroupService = userGroupService;
        this.passwordEncoder = passwordEncoder;
        this.passwordValidator = passwordValidator;
        this.userNameValidator = userNameValidator;
        this.emailValidator = emailValidator;
    }

    @Override
    protected void doInit() {
        runAsSystemWithoutResult(new Initializer());
    }

    private boolean isQueryPresent(final PageRequest pageRequest) {
        final int minLen = userProfileProperties.getMinQueryLengthForUnprivilegedUsers();
        return pageRequest != null && pageRequest.getQuery() != null && pageRequest.getQuery().length() >= minLen;
    }

    private boolean isPrivilegedReader() {
        return isCurrentUserAdminOrSystem()
                || getCurrentUserRoles().contains(new SimpleGrantedAuthority(RoleDto.READ_ALL_PROFILES_ROLE_NAME));
    }

    @PreAuthorize("isAuthenticated()")
    @Override
    public Page<UserProfileDto> findAll(final PageRequest request) {

        final PageRequest pageRequest = request == null ? new PageRequestDto() : request;

        log.info("{}: Find all users with page request [{}] ...", getCurrentUserName(), pageRequest);

        if (!isPrivilegedReader() && !isQueryPresent(pageRequest)) {
            final int minLen = userProfileProperties.getMinQueryLengthForUnprivilegedUsers();
            ForbiddenException e = new ForbiddenException(
                    String.format("The length of the query must have a minimum of %d", minLen));
            if (log.isErrorEnabled()) {
                log.error(String.format("Find all users with page request [%s] failed.", pageRequest), e);
            }
            throw e;
        }

        SpringPageRequest pageable = PageBuilderSpringUtils.toSpringPageRequest(pageRequest);
        org.springframework.data.domain.Page<UserProfileMongo> springPage;
        if (StringUtils.isBlank(pageRequest.getQuery())) {
            springPage = userProfileMongoRepository.findAll(pageable);
        } else {
            springPage = userProfileMongoRepository.findBySearchRegex(pageRequest.getQuery(), pageable);
        }
        Page<UserProfileDto> dtos = PageBuilderSpringUtils.fromSpringPage(springPage, userProfileMongoMapper::mapToDto);
        log.info("{}: Find all users with page request [{}]: Returning page no. {} with {} entries.",
                getCurrentUserName(), pageRequest, dtos.getPageRequest().getPageNumber(), dtos.getEntries().size());
        return dtos;
    }

    @PreAuthorize("isAuthenticated()")
    @PostAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SYSTEM','ROLE_READ_ALL_PROFILES')"
            + " or hasPermission(#returnObject.uid, 'UserProfile', 'read')")
    @Override
    public UserProfileDto findByIdentifier(final String identifier) {

        log.info("{}: Find user by identifier [{}] ...", getCurrentUserName(), identifier);
        BadRequestException.validateNotBlank(identifier, "Identifier must be present.");
        UserProfileLdap ldap = userProfileLdapDao.findByUserName(identifier);
        Long uidNumber;
        try {
            uidNumber = Long.parseLong(identifier);
        } catch (NumberFormatException ignored) {
            uidNumber = null;
        }
        if (ldap == null) {
            ldap = userProfileLdapDao.findByEmail(identifier);
        }
        if (ldap == null && uidNumber != null) {
            ldap = userProfileLdapDao.findByUidNumber(uidNumber);
        }
        // sambaSID is possible, too
        NotFoundException.validateNotNull(ldap, String.format("User with identifier [%s] was not found.", identifier));
        UserProfileMongo entity = findOrCreateUserProfileMongo(ldap);
        UserProfileDto dto = userProfileMongoMapper.mapToDto(entity);
        log.info("{}: Find user by identifier [{}]: DONE!", getCurrentUserName(), identifier);
        return dto;
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SYSTEM','ROLE_READ_ALL_PROFILES')"
            + " or hasPermission(#userName, 'UserProfile', 'read')")
    @Override
    public UserProfileDto findByUserName(final String userName) {

        log.info("{}: Find user by name [{}] ...", getCurrentUserName(), userName);
        UserProfileMongo entity = loadUserProfileMongo(userName);
        UserProfileDto dto = userProfileMongoMapper.mapToDto(entity);
        log.info("{}: Find user by name [{}]: DONE!", getCurrentUserName(), userName);
        return dto;
    }

    @PreAuthorize("isAuthenticated()")
    @PostAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SYSTEM','ROLE_READ_ALL_PROFILES')"
            + " or hasPermission(#returnObject.uid, 'UserProfile', 'read')")
    @Override
    public UserProfileDto findByUidNumber(final long uidNumber) {
        log.info("{}: Find user by UID number [{}] ...", getCurrentUserName(), uidNumber);
        UserProfileLdap ldap = userProfileLdapDao.findByUidNumber(uidNumber);
        NotFoundException.validateNotNull(ldap,
                String.format("User with UID number [%s] was not found.", uidNumber));
        UserProfileMongo entity = findOrCreateUserProfileMongo(ldap);
        UserProfileDto dto = userProfileMongoMapper.mapToDto(entity);
        log.info("{}: Find user group by UID number [{}]: DONE!", getCurrentUserName(), uidNumber);
        return dto;
    }

    @PreAuthorize("isAuthenticated()")
    @PostAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SYSTEM','ROLE_READ_ALL_PROFILES')"
            + " or hasPermission(#returnObject.uid, 'UserProfile', 'read')")
    @Override
    public UserProfileDto findByEmail(final String email) {

        log.info("{}: Find user by email [{}] ,,,", getCurrentUserName(), email);
        BadRequestException.validateNotBlank(email, EMAIL_NAME_MUST_BE_PRESENT);
        UserProfileMongo entity = userProfileMongoRepository.findByEmail(email);
        if (entity == null) {
            UserProfileLdap ldap = userProfileLdapDao.findByEmail(email);
            if (ldap != null) {
                entity = findOrCreateUserProfileMongo(ldap);
            }
        }
        NotFoundException.validateNotNull(entity, String.format("User with email [%s] was not found.", email));
        UserProfileDto dto = userProfileMongoMapper.mapToDto(entity);
        log.info("{}: Find user by email [{}]: DONE!", getCurrentUserName(), email);
        return dto;
    }

    @Override
    public boolean existsByIdentifier(String identifier) {
        log.info("{}: User with identifier [{}] exists? ...", getCurrentUserName(), identifier);
        BadRequestException.validateNotBlank(identifier, "Identifier must be present.");
        Long uidNumber;
        try {
            uidNumber = Long.parseLong(identifier);
        } catch (NumberFormatException ignored) {
            uidNumber = null;
        }
        // sambaSID is possible, too
        final boolean result = userProfileLdapDao.existsByUserName(identifier)
                || userProfileLdapDao.existsByEmail(identifier)
                || (uidNumber != null && userProfileLdapDao.existsByUidNumber(uidNumber));
        log.info("{}: User with identifier [{}] exists? {}", getCurrentUserName(), identifier, result);
        return result;
    }

    @PreAuthorize("isAuthenticated()")
    @Override
    public boolean existsByUserName(final String userName) {
        log.info("{}: User with name [{}] exists? ...", getCurrentUserName(), userName);
        BadRequestException.validateNotBlank(userName, USER_NAME_MUST_BE_PRESENT);
        final boolean result = userProfileLdapDao.existsByUserName(userName);
        log.info("{}: User with name [{}] exists? {}", getCurrentUserName(), userName, result);
        return result;
    }

    @PreAuthorize("isAuthenticated()")
    @Override
    public boolean existsByUidNumber(final long uidNumber) {
        log.info("{}: User with UID number [{}] exists? ...", getCurrentUserName(), uidNumber);
        final boolean result = userProfileLdapDao.existsByUidNumber(uidNumber);
        log.info("{}: User with UID number [{}] exists? {}", getCurrentUserName(), uidNumber, result);
        return result;
    }

    @PreAuthorize("isAuthenticated()")
    @Override
    public boolean existsByEmail(final String email) {
        log.info("{}: User with email [{}] exists? ...", getCurrentUserName(), email);
        BadRequestException.validateNotBlank(email, EMAIL_NAME_MUST_BE_PRESENT);
        final boolean result = userProfileLdapDao.existsByEmail(email);
        log.info("{}: User with email [{}] exists? {}", getCurrentUserName(), email, result);
        return result;
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SYSTEM')"
            + " or hasPermission(#userName, 'UserProfile', 'delete')")
    @Override
    public void deleteByUserName(final String userName) {

        log.info("{}: Deleting user with name [{}] ...", getCurrentUserName(), userName);
        BadRequestException.validateNotBlank(userName, USER_NAME_MUST_BE_PRESENT);
        runAsSystemWithoutResult(new RunAsCallbackWithoutResult() {
            @Override
            public void run() {
                for (String group : userGroupService.findUserGroupNamesByMember(userName)) {
                    userGroupService.removeMembers(group, Collections.singleton(userName));
                }
                for (String role : roleService.findRoleNamesByMember(userName)) {
                    roleService.removeMembers(role, Collections.singleton(userName));
                }
                friendsService.deleteFriendsRole(userName);
                roleService.deleteCustomRoles(userName);

                deleteAcls(new ObjectIdentityImpl(UserProfileDto.TYPE_ID, userName), true);
            }
        });
        userProfileLdapDao.deleteByUserName(userName);
        userProfileMongoRepository.deleteByUid(userName);
        log.info("{}: Deleting user with name [{}]: DONE!", getCurrentUserName(), userName);
    }

    @PreAuthorize("hasRole('ROLE_SYSTEM')")
    @Override
    public UserProfileDto create(final UserRegistrationDto userRegistration) {

        BadRequestException.validateNotNull(userRegistration, "User registration must be present.");
        BadRequestException.validateNotBlank(userRegistration.getUid(), USER_NAME_MUST_BE_PRESENT);
        log.info("{}: Registering user with name [{}] ...", getCurrentUserName(), userRegistration.getUid());
        BadRequestException.validateNotBlank(userRegistration.getEmail(), EMAIL_NAME_MUST_BE_PRESENT);
        BadRequestException.validateNotBlank(userRegistration.getFirstName(), "First name must be present.");
        BadRequestException.validateNotBlank(userRegistration.getLastName(), "Last name must be present.");
        BadRequestException.validateNotBlank(userRegistration.getPassword(), "Password must be present.");
        BadRequestException.validateNotBlank(userRegistration.getSambaLmPassword(),
                "Samba LM Password must be present.");
        BadRequestException.validateNotBlank(userRegistration.getSambaNtPassword(),
                "Samba NT Password must be present.");

        // User name and email address should be already validated.
        UserProfileLdap ldap = userProfileLdapMapper.mapToEntity(userRegistration);
        UserProfileMongo mongo = userProfileMongoMapper.mapToEntity(userRegistration);
        UserProfileDto dto = create(ldap, mongo);
        log.info("{}: Registering user with name [{}]: DONE!", getCurrentUserName(), userRegistration.getUid());
        return dto;
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SYSTEM')")
    @Override
    public UserProfileDto create(final UserProfileCreateRequestDto request) {

        BadRequestException.validateNotNull(request, "User registration request must be present.");
        BadRequestException.validateNotBlank(request.getUid(), USER_NAME_MUST_BE_PRESENT);
        log.info("{}: Creating user with name [{}] ...", getCurrentUserName(), request.getUid());
        BadRequestException.validateNotBlank(request.getEmail(), EMAIL_NAME_MUST_BE_PRESENT);
        BadRequestException.validateNotBlank(request.getFirstName(), "First name must be present.");
        BadRequestException.validateNotBlank(request.getLastName(), "Last name must be present.");

        userNameValidator.validateNew(request.getUid(), ValidatorConstants.USER_AND_OAUTH2_CLIENT_TABLE_MASK);
        emailValidator.validateNew(request.getEmail(), true, ValidatorConstants.ALL_TABLE_MASK);
        passwordValidator.validate(request.getPassword(), null, null, null);

        UserProfileLdap ldap = userProfileLdapMapper.mapToEntity(request);
        UserProfileMongo mongo = userProfileMongoMapper.mapToEntity(request);
        UserProfileDto dto = create(ldap, mongo);
        log.info("{}: Creating user with name [{}]: DONE!", getCurrentUserName(), request.getUid());
        return dto;
    }

    private UserProfileDto create(UserProfileLdap userProfileLdap, UserProfileMongo userProfileMongo) {
        UserProfileLdap ldap = userProfileLdapDao.save(userProfileLdap);
        UserProfileMongo entity = userProfileMongoRepository.save(userProfileMongo);
        initUserProfile(ldap);
        return userProfileMongoMapper.mapToDto(entity);
    }

    private void initUserProfile(UserProfileLdap ldap) {
        initUserProfileRoles(ldap);
        initUserProfileAcls(ldap);
    }

    private void initUserProfileRoles(UserProfileLdap ldap) {
        // Add user to roles.
        Set<String> roles = new HashSet<>();
        roles.add(RoleDto.USER_ROLE_NAME);
        roles.addAll(userProfileProperties.getDefaultUserRoles());
        for (String role : roles) {
            if (!roleService.existsByName(role)) {
                RoleDto createRoleRequest = new RoleDto();
                createRoleRequest.setName(role);
                createRoleRequest.setDescription("Role was created during creation of OAuth2Client with name ["
                        + ldap.getUid() + "].");
                roleService.create(createRoleRequest);
            }
            roleService.addMembers(role, Collections.singleton(ldap.getUid()));
        }
        // create friends role
        if (!roleService.existsByName(roleNameService.createFriendsRoleName(ldap.getUid()))) {
            friendsService.createFriendsRole(ldap.getUid());
        }
        // The default user group will be created at 'applyPosixSettingsToProfile'.
    }

    private void initUserProfileAcls(UserProfileLdap ldap) {

        final PrincipalSid ownerSid = new PrincipalSid(ldap.getUid());
        MutableAcl acl;
        try {
            acl = (MutableAcl) getAclService().readAclById(getObjectIdentityRetrievalStrategy()
                    .getObjectIdentity(ldap));

        } catch (org.springframework.security.acls.model.NotFoundException e) { // NOSONAR

            acl = initAcl(ldap, ldap.getUid());
            acl.insertAce(acl.getEntries().size(), BasePermission.ADMINISTRATION, ownerSid, true);
            acl.insertAce(acl.getEntries().size(), BasePermission.CREATE, ownerSid, true);
            acl.insertAce(acl.getEntries().size(), BasePermission.DELETE, ownerSid, true);
            acl.insertAce(acl.getEntries().size(), BasePermission.READ, ownerSid, true);
            acl.insertAce(acl.getEntries().size(), BasePermission.WRITE, ownerSid, true);

            final String friendsRoleName = roleNameService.createFriendsRoleName(ldap.getUid());
            final GrantedAuthoritySid friendsSid = new GrantedAuthoritySid(friendsRoleName);
            acl.insertAce(acl.getEntries().size(), BasePermission.READ, friendsSid, true);
            acl = getAclService().updateAcl(acl);
        }

        final String[] attributeNames = {
                MailSettingsDto.USER_PROFILE_ATTRIBUTE_NAME,
                OrganisationSettingsDto.USER_PROFILE_ATTRIBUTE_NAME,
                PosixSettingsDto.USER_PROFILE_ATTRIBUTE_NAME,
                SambaSettingsDto.USER_PROFILE_ATTRIBUTE_NAME
        };
        for (final String attribuetName : attributeNames) {
            final ObjectIdentity objectIdentity = createObjectIdentityWithAttribute(
                    ldap.getUid(),
                    UserProfileDto.TYPE_ID,
                    attribuetName);
            try {
                getAclService().readAclById(objectIdentity);

            } catch (org.springframework.security.acls.model.NotFoundException e) { // NOSONAR
                MutableAcl mailSettingsAcl = initAcl(objectIdentity, getSystemName(), acl, false);
                mailSettingsAcl.insertAce(mailSettingsAcl.getEntries().size(), BasePermission.READ, ownerSid, true);
                getAclService().updateAcl(mailSettingsAcl);
            }
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SYSTEM')"
            + " or hasPermission(#userName, 'UserProfile', 'write')")
    @Override
    public UserProfileDto update(final String userName, final UserProfileDto userProfile) {

        BadRequestException.validateNotBlank(userName, USER_NAME_MUST_BE_PRESENT);
        BadRequestException.validateNotNull(userProfile, "User profile must be present.");
        userProfile.setUid(userName);
        log.info("{}: Updating user [{}] ...", getCurrentUserName(), userProfile);
        UserProfileLdap ldap = loadUserProfileLdap(userName);
        UserProfileMongo mongo = findOrCreateUserProfileMongo(ldap);
        userProfileLdapMapper.updateEntity(userProfile, ldap);
        userProfileLdapDao.save(ldap);
        userProfileMongoMapper.updateEntity(userProfile, mongo);
        mongo = userProfileMongoRepository.save(mongo);
        UserProfileDto dto = userProfileMongoMapper.mapToDto(mongo);
        log.info("{}: Updating user [{}]: DONE!", getCurrentUserName(), dto);
        return dto;
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SYSTEM')"
            + " or hasPermission(#userName, 'UserProfile', 'write')")
    @Override
    public void changePassword(final String userName, final String newPassword, final String oldPassword) {
        log.info("{}: Changing password of user [{}] ...", getCurrentUserName(), userName);
        doChangePassword(userName, newPassword, oldPassword);
        log.info("{}: Changing password of user [{}]: DONE!", getCurrentUserName(), userName);
    }

    private void doChangePassword(final String userName, final String newPassword, final String oldPassword) {

        UserProfileLdap ldap = loadUserProfileLdap(userName);
        passwordValidator.validate(newPassword, ldap.getPassword(), oldPassword, null);
        ldap.setPassword(passwordEncoder.encode(newPassword));
        ldap.setSambaLmPassword(passwordEncoder.createSambaLMPassword(newPassword));
        ldap.setSambaNtPassword(passwordEncoder.createSambaNTPassword(newPassword));
        SambaSettingsDto sambaSettings = ldap.getSambaSettings();
        if (sambaSettings == null) {
            sambaSettings = new SambaSettingsDto();
            ldap.setSambaSettings(sambaSettings);
        }
        sambaSettings.setSambaPwdLastSet((int) (System.currentTimeMillis() / 1000L));
        userProfileLdapDao.save(ldap);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SYSTEM')")
    @Override
    public void resetPassword(final String userName, final String newPassword) {
        log.info("{}: Resetting password of user [{}] ...", getCurrentUserName(), userName);
        doChangePassword(userName, newPassword, null);
        log.info("{}: Resetting password of user [{}]: DONE!", getCurrentUserName(), userName);
    }

    private class Initializer extends RunAsCallbackWithoutResult {

        @Override
        public void run() {
            if (userProfileProperties.isCheckAllProfilesAtStartup()) {
                List<UserProfileLdap> ldaps = userProfileLdapDao.findAll(new PageRequestDto()).getEntries();
                for (UserProfileLdap ldap : ldaps) {
                    findOrCreateUserProfileMongo(ldap);
                    initUserProfile(ldap);
                }
            }

            if (!existsByUserName(getAdminName())) {
                UserProfileCreateRequestDto request = new UserProfileCreateRequestDto();
                request.setPassword(userProfileProperties.getAdminPassword());
                request.setUid(getAdminName());
                request.setEmail(getAdminProfile().getEmail());
                request.setFirstName(getAdminProfile().getFirstName());
                request.setLastName(getAdminProfile().getLastName());
                create(request);
            }
            roleService.addMembers(RoleDto.ADMIN_ROLE_NAME, Collections.singleton(getAdminName()));
            roleService.addMembers(RoleDto.ACL_ADMIN_ROLE_NAME, Collections.singleton(getAdminName()));
        }
    }

}
