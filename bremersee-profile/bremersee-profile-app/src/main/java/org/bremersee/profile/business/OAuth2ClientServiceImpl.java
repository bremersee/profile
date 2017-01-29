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
import org.bremersee.common.exception.BadRequestException;
import org.bremersee.common.exception.NotFoundException;
import org.bremersee.common.security.core.context.RunAsCallbackWithoutResult;
import org.bremersee.common.security.crypto.password.PasswordEncoder;
import org.bremersee.pagebuilder.model.Page;
import org.bremersee.pagebuilder.model.PageRequest;
import org.bremersee.pagebuilder.model.PageRequestDto;
import org.bremersee.pagebuilder.spring.PageBuilderSpringUtils;
import org.bremersee.pagebuilder.spring.SpringPageRequest;
import org.bremersee.profile.domain.ldap.dao.OAuth2ClientLdapDao;
import org.bremersee.profile.domain.mongodb.entity.OAuth2ClientMongo;
import org.bremersee.profile.domain.mongodb.mapper.OAuth2ClientMongoMapper;
import org.bremersee.profile.domain.mongodb.repository.OAuth2ClientMongoRepository;
import org.bremersee.profile.model.OAuth2ClientCreateRequestDto;
import org.bremersee.profile.model.OAuth2ClientDto;
import org.bremersee.profile.model.RoleDto;
import org.bremersee.profile.validation.PasswordValidator;
import org.bremersee.profile.validation.UserNameValidator;
import org.bremersee.profile.validation.ValidatorConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Christian Bremer
 */
@SuppressWarnings("unused")
@Service("oAuth2ClientService")
@EnableConfigurationProperties(OAuth2ClientServiceProperties.class)
public class OAuth2ClientServiceImpl extends AbstractServiceImpl implements OAuth2ClientService {

    private static final String CLIENT_ID_PRESENT = "OAuth2 client ID must be present.";

    private final OAuth2ClientLdapDao oAuth2ClientLdapDao;

    private final OAuth2ClientMongoRepository oAuth2ClientMongoRepository;

    private final OAuth2ClientMongoMapper oAuth2ClientMongoMapper;

    private final RoleService roleService;

    private final PasswordEncoder passwordEncoder;

    private final PasswordValidator passwordValidator;

    private final UserNameValidator userNameValidator;

    private final OAuth2ClientServiceProperties properties;

    @Autowired
    public OAuth2ClientServiceImpl(OAuth2ClientLdapDao oAuth2ClientLdapDao, // NOSONAR
                                   OAuth2ClientMongoRepository oAuth2ClientMongoRepository,
                                   OAuth2ClientMongoMapper oAuth2ClientMongoMapper,
                                   RoleService roleService,
                                   PasswordEncoder passwordEncoder,
                                   PasswordValidator passwordValidator,
                                   UserNameValidator userNameValidator,
                                   @SuppressWarnings("SpringJavaAutowiringInspection") OAuth2ClientServiceProperties properties) {

        this.oAuth2ClientLdapDao = oAuth2ClientLdapDao;
        this.oAuth2ClientMongoRepository = oAuth2ClientMongoRepository;
        this.oAuth2ClientMongoMapper = oAuth2ClientMongoMapper;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
        this.passwordValidator = passwordValidator;
        this.userNameValidator = userNameValidator;
        this.properties = properties;
    }

    @Override
    protected void doInit() {
        runAsSystemWithoutResult(new RunAsCallbackWithoutResult() {
            @Override
            public void run() {
                OAuth2ClientCreateRequestDto client = properties.getSystemClient();
                if (!existsByClientId(client.getClientId())) {
                    create(client);
                }
                client = properties.getSwaggerUiClient();
                if (!existsByClientId(client.getClientId())) {
                    create(client);
                }
                for (OAuth2ClientCreateRequestDto other : properties.getOtherClients()) {
                    if (!existsByClientId(other.getClientId())) {
                        create(other);
                    }
                }
            }
        });
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SYSTEM','ROLE_READ_ALL_PROFILES')")
    @Override
    public Page<OAuth2ClientDto> findAll(final PageRequest request) {

        final PageRequest pageRequest = request == null ? new PageRequestDto() : request;
        final SpringPageRequest pageable = PageBuilderSpringUtils.toSpringPageRequest(pageRequest);
        org.springframework.data.domain.Page<OAuth2ClientMongo> springPage;
        if (StringUtils.isBlank(pageRequest.getQuery())) {
            springPage = oAuth2ClientMongoRepository.findAll(pageable);
        } else {
            springPage = oAuth2ClientMongoRepository.findBySearchRegex(pageRequest.getQuery(), pageable);
        }
        return PageBuilderSpringUtils.fromSpringPage(springPage, oAuth2ClientMongoMapper::mapToDto);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SYSTEM')")
    @Override
    public OAuth2ClientDto create(final OAuth2ClientCreateRequestDto client) {

        BadRequestException.validateNotNull(client, "Create client request must not be null.");
        BadRequestException.validateNotBlank(client.getClientId(), CLIENT_ID_PRESENT);

        if (log.isDebugEnabled()) {
            log.debug("{}: Creating oauth2 client [{}] ...", getCurrentUserName(), client);
        } else {
            log.info("{}: Creating oauth2 client [{}] ...", getCurrentUserName(), client.getClientId());
        }

        userNameValidator.validateNew(client.getClientId(), ValidatorConstants.ALL_TABLE_MASK);
        passwordValidator.validate(client.getClientSecret(), null, null, null);

        OAuth2ClientMongo entity = new OAuth2ClientMongo();
        entity.setClientId(client.getClientId());
        oAuth2ClientMongoMapper.updateEntity(client, entity);
        if (StringUtils.isNotBlank(client.getClientSecret())) {
            entity.setClientSecret(passwordEncoder.encode(client.getClientSecret()));
        }

        entity = oAuth2ClientMongoRepository.save(entity);
        oAuth2ClientLdapDao.save(entity.getClientId());

        Set<String> roles = new HashSet<>();
        roles.add(RoleDto.OAUTH2_CLIENT_ROLE_NAME);
        if (client.getRoles() != null) {
            roles.addAll(client.getRoles());
        }
        for (String role : roles) {
            if (!roleService.existsByName(role)) {
                RoleDto createRoleRequest = new RoleDto();
                createRoleRequest.setName(role);
                createRoleRequest.setDescription("Role was created during creation of OAuth2Client with name ["
                        + client.getClientId() + "].");
                roleService.create(createRoleRequest);
            }
            roleService.addMembers(role, Collections.singleton(client.getClientId()));
        }

        initAcl(entity);
        OAuth2ClientDto dto = oAuth2ClientMongoMapper.mapToDto(entity);

        log.info("{}: Creating oauth2 client [" + client.getClientId() + "] ... DONE!"); // NOSONAR
        return dto;
    }

    private OAuth2ClientMongo getOAuth2ClientEntity(final String clientId) {
        OAuth2ClientMongo entity = oAuth2ClientMongoRepository.findByClientId(clientId);
        if (entity == null) {
            NotFoundException e = new NotFoundException("OAuth2 client [" + clientId
                    + "] was not found."); // NOSONAR
            log.error(getCurrentUserName() + ": Updating oauth2 client failed.", e);
            throw e;
        }
        return entity;
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SYSTEM')"
            + " or hasPermission(#clientId, 'OAuth2Client', 'write')")
    @Override
    public OAuth2ClientDto update(final String clientId, final OAuth2ClientDto client) {

        log.info("{}: Updating oauth2 client [{}] ...", getCurrentUserName(), clientId);

        BadRequestException.validateNotBlank(clientId, CLIENT_ID_PRESENT);
        BadRequestException.validateNotNull(client, "OAuth2 client must not be null.");
        client.setClientId(clientId);

        OAuth2ClientMongo entity = getOAuth2ClientEntity(clientId);
        oAuth2ClientMongoMapper.updateEntity(client, entity);
        entity = oAuth2ClientMongoRepository.save(entity);
        OAuth2ClientDto dto = oAuth2ClientMongoMapper.mapToDto(entity);

        log.info("{}: Updating oauth2 client [{}] ... DONE!", clientId);
        return dto;
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SYSTEM')"
            + " or hasPermission(#clientId, 'OAuth2Client', 'write')")
    @Override
    public void resetPassword(final String clientId, final String newPassword, final boolean skipValidation) {

        log.info("{}: Resetting password of oauth2 client [{}] ...", getCurrentUserName(), clientId);

        BadRequestException.validateNotBlank(clientId, CLIENT_ID_PRESENT);
        OAuth2ClientMongo entity = getOAuth2ClientEntity(clientId);
        if (!skipValidation) {
            passwordValidator.validate(newPassword, null, null, null);
        }
        if (StringUtils.isBlank(newPassword)) {
            entity.setClientSecret(null);
        } else {
            entity.setClientSecret(passwordEncoder.encode(newPassword));
        }
        oAuth2ClientMongoRepository.save(entity);

        log.info("{}: Resetting password of oauth2 client [{}] ... DONE!", getCurrentUserName(), clientId);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SYSTEM')"
            + " or #clientId == authentication.name")
    @Override
    public void changePassword(final String clientId, final String newPassword, final String oldPassword) {

        log.info("{}: Changing password of oauth2 client [{}] ...", getCurrentUserName(), clientId);

        BadRequestException.validateNotBlank(clientId, CLIENT_ID_PRESENT);
        OAuth2ClientMongo entity = getOAuth2ClientEntity(clientId);
        passwordValidator.validate(newPassword, entity.getClientSecret(), oldPassword, null);
        if (StringUtils.isBlank(newPassword)) {
            entity.setClientSecret(null);
        } else {
            entity.setClientSecret(passwordEncoder.encode(newPassword));
        }
        oAuth2ClientMongoRepository.save(entity);

        log.info("{}: Changing password of oauth2 client [{}] ... DONE!", getCurrentUserName(), clientId);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SYSTEM','ROLE_READ_ALL_PROFILES')"
            + " or #clientId == authentication.name")
    @Override
    public OAuth2ClientDto findByClientId(final String clientId) {

        log.info("{}: Getting oauth2 client [{}] ...", getCurrentUserName(), clientId);
        BadRequestException.validateNotBlank(clientId, CLIENT_ID_PRESENT);
        return oAuth2ClientMongoMapper.mapToDto(getOAuth2ClientEntity(clientId));
    }

    @PreAuthorize("isAuthenticated()")
    @Override
    public boolean existsByClientId(final String clientId) {

        final boolean result = oAuth2ClientMongoRepository.findByClientId(clientId) != null;
        log.info("{}: OAuth2 client [{}] exists? {}", getCurrentUserName(), clientId, result);
        return result;
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SYSTEM')"
            + " or hasPermission(#clientId, 'OAuth2Client', 'delete')")
    @Override
    public void deleteByClientId(final String clientId) {

        log.info("{}: Deleting oauth2 client [{}] ...", getCurrentUserName(), clientId);
        runAsSystemWithoutResult(new RunAsCallbackWithoutResult() {
            @Override
            public void run() {
                for (String role : roleService.findRoleNamesByMember(clientId)) {
                    roleService.removeMembers(role, Collections.singleton(clientId));
                }
                deleteAcls(new ObjectIdentityImpl(OAuth2ClientDto.TYPE_ID, clientId), true);
            }
        });

        oAuth2ClientMongoRepository.deleteByClientId(clientId);
        oAuth2ClientLdapDao.delete(clientId);
        log.info("{}: Deleting oauth2 client [{}] ... DONE!", getCurrentUserName(), clientId);
    }

}
