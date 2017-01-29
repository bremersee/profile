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
import org.bremersee.common.exception.AlreadyExistsException;
import org.bremersee.common.exception.BadRequestException;
import org.bremersee.common.exception.NotFoundException;
import org.bremersee.common.security.core.context.RunAsCallbackWithoutResult;
import org.bremersee.pagebuilder.PageBuilderUtils;
import org.bremersee.pagebuilder.model.Page;
import org.bremersee.pagebuilder.model.PageRequest;
import org.bremersee.pagebuilder.model.PageRequestDto;
import org.bremersee.profile.domain.ldap.dao.SambaDomainLdapDao;
import org.bremersee.profile.domain.ldap.entity.SambaDomainLdap;
import org.bremersee.profile.domain.ldap.mapper.SambaDomainLdapMapper;
import org.bremersee.profile.model.SambaDomainDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.stereotype.Service;

/**
 * @author Christian Bremer
 */
@Service("sambaDomainService")
@EnableConfigurationProperties(SambaDomainProperties.class)
public class SambaDomainServiceImpl extends AbstractServiceImpl implements SambaDomainService {

    private final SambaDomainLdapDao sambaDomainLdapDao;

    private final SambaDomainLdapMapper sambaDomainLdapMapper;

    private final SambaDomainProperties sambaDomainProperties;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    public SambaDomainServiceImpl(
            SambaDomainLdapDao sambaDomainLdapDao,
            SambaDomainLdapMapper sambaDomainLdapMapper,
            SambaDomainProperties sambaDomainProperties) {

        this.sambaDomainLdapDao = sambaDomainLdapDao;
        this.sambaDomainLdapMapper = sambaDomainLdapMapper;
        this.sambaDomainProperties = sambaDomainProperties;
    }

    @Override
    protected void doInit() {
        runAsSystemWithoutResult(new Initializer());
    }

    @Override
    public String getDefaultSambaDomainName() {
        return sambaDomainProperties.getDefaultSambaDomain().getSambaDomainName();
    }

    @Override
    public String getDefaultSambaSID() {
        return sambaDomainProperties.getDefaultSambaDomain().getSambaDomainName();
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SYSTEM')")
    @Override
    public SambaDomainDto getDefaultSambaDomain() {
        SambaDomainDto tmp = findDefaultSambaDomain();
        if (tmp instanceof SambaDomainLdap) {
            return sambaDomainLdapMapper.mapToDto((SambaDomainLdap) tmp);
        }
        // Create a clone of the default.
        SambaDomainLdap tmpEntity = new SambaDomainLdap();
        sambaDomainLdapMapper.updateEntity(tmp, tmpEntity);
        return sambaDomainLdapMapper.mapToDto(tmpEntity);
    }

    private SambaDomainDto findDefaultSambaDomain() {
        SambaDomainLdap entity = sambaDomainLdapDao.findBySambaDomainName(getDefaultSambaDomainName());
        if (entity != null) {
            return entity;
        }
        return sambaDomainProperties.getDefaultSambaDomain();
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SYSTEM')")
    @Override
    public long getNextUidNumber() {
        SambaDomainDto tmp = findDefaultSambaDomain();
        long uid = tmp.getUidNumber() == null ? 10000L : tmp.getUidNumber();
        tmp.setUidNumber(uid + 1L);
        if (tmp instanceof SambaDomainLdap) {
            sambaDomainLdapDao.save((SambaDomainLdap) tmp);
        }
        return uid;
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SYSTEM')")
    @Override
    public long getNextGidNumber() {
        SambaDomainDto tmp = findDefaultSambaDomain();
        long gid = tmp.getGidNumber() == null ? 10000L : tmp.getGidNumber();
        tmp.setGidNumber(gid + 1L);
        if (tmp instanceof SambaDomainLdap) {
            sambaDomainLdapDao.save((SambaDomainLdap) tmp);
        }
        return gid;
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SYSTEM')")
    @Override
    public String getDefaultSambaSID(long gidOrUidNumber) {
        SambaDomainDto sambaDomain = findBySambaDomainName(getDefaultSambaDomainName());
        return getSambaSID(gidOrUidNumber, sambaDomain);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SYSTEM')")
    @Override
    public String getSambaSID(long gidOrUidNumber, final String sambaDomainNameOrSambaSID) {
        BadRequestException.validateNotBlank(sambaDomainNameOrSambaSID,
                "Samba domain name or Samba SID must be present.");
        SambaDomainLdap entity = sambaDomainLdapDao.findBySambaDomainNameOrSambaSID(sambaDomainNameOrSambaSID);
        NotFoundException.validateNotNull(entity,
                String.format("Samba domain [%s] not found.", sambaDomainNameOrSambaSID));

        return getSambaSID(gidOrUidNumber, entity);
    }

    private String getSambaSID(long gidOrUidNumber, final SambaDomainDto sambaDomain) {
        final long sambaAlgorithmicRidBase;
        if (sambaDomain != null && sambaDomain.getSambaAlgorithmicRidBase() != null) {
            sambaAlgorithmicRidBase = sambaDomain.getSambaAlgorithmicRidBase();
        } else {
            sambaAlgorithmicRidBase = sambaDomainProperties.getDefaultSambaDomain().getSambaAlgorithmicRidBase();
        }
        final String sambaSID;
        if (sambaDomain != null && StringUtils.isNotBlank(sambaDomain.getSambaSID())) {
            sambaSID = sambaDomain.getSambaSID();
        } else {
            sambaSID = sambaDomainProperties.getDefaultSambaDomain().getSambaSID();
        }
        return sambaSID + "-" + (sambaAlgorithmicRidBase + (2 * gidOrUidNumber));
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SYSTEM')")
    @Override
    public SambaDomainDto create(final SambaDomainDto sambaDomain) {

        log.info("{}: Creating samba domain [{}] ...", getCurrentUserName(), sambaDomain);

        BadRequestException.validateNotNull(sambaDomain, "Samba domain must not be null.");
        BadRequestException.validateNotBlank(sambaDomain.getSambaDomainName(),
                "Samba domain name must be present."); // NOSONAR
        BadRequestException.validateNotBlank(sambaDomain.getSambaSID(), "Samba SID must be present.");

        if (sambaDomainLdapDao.existsBySambaDomainName(sambaDomain.getSambaDomainName())) {
            AlreadyExistsException e = new AlreadyExistsException(
                    String.format("Samba domain with domain name [%s] already exists.",
                            sambaDomain.getSambaDomainName()));
            if (log.isErrorEnabled()) {
                log.error(String.format("%s: Creating samba domain failed.", getCurrentUserName()), e);
            }
            throw e;
        }
        if (sambaDomainLdapDao.existsBySambaSID(sambaDomain.getSambaSID())) {
            AlreadyExistsException e = new AlreadyExistsException(
                    String.format("Samba domain with SID [%s] already exists.",
                            sambaDomain.getSambaSID()));
            if (log.isErrorEnabled()) {
                log.error(String.format("%s: Creating samba domain failed.", getCurrentUserName()), e);
            }
            throw e;
        }

        SambaDomainLdap entity = new SambaDomainLdap();
        entity.setSambaDomainName(sambaDomain.getSambaDomainName());
        sambaDomainLdapMapper.updateEntity(sambaDomain, entity);
        entity = sambaDomainLdapDao.save(entity);
        initAcl(entity);
        SambaDomainDto dto = sambaDomainLdapMapper.mapToDto(entity);
        log.info("{}: Creating samba domain [{}]: DONE!", getCurrentUserName(), sambaDomain);
        return dto;
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SYSTEM')"
            + " or hasPermission(#sambaDomainName, 'SambaDomain', 'write')")
    @Override
    public SambaDomainDto update(final String sambaDomainName, final SambaDomainDto sambaDomain) {
        log.info("{}: Updating samba domain with name [{}] ...", getCurrentUserName(), sambaDomainName);
        BadRequestException.validateNotBlank(sambaDomainName, "Samba domain name must be present.");
        BadRequestException.validateNotNull(sambaDomain, "Samba domain must not be null.");
        sambaDomain.setSambaDomainName(sambaDomainName);
        SambaDomainLdap entity = sambaDomainLdapDao.findBySambaDomainName(sambaDomainName);
        NotFoundException.validateNotNull(entity, "Samba domain with name [" + sambaDomainName + "] was not found.");
        sambaDomainLdapMapper.updateEntity(sambaDomain, entity);
        entity = sambaDomainLdapDao.save(entity);
        SambaDomainDto dto = sambaDomainLdapMapper.mapToDto(entity);
        log.info("{}: Updating samba domain with name [{}]: DONE!", getCurrentUserName(), sambaDomainName);
        return dto;
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SYSTEM','ROLE_READ_ALL_PROFILES')")
    @Override
    public Page<SambaDomainDto> findAll(final PageRequest request) {
        final PageRequest pageRequest = request == null ? new PageRequestDto() : request;
        log.info("{}: Find all samba domains with page request [{}] ...", getCurrentUserName(), pageRequest);
        Page<SambaDomainLdap> entities = sambaDomainLdapDao.findAll(pageRequest);
        Page<SambaDomainDto> dtos = PageBuilderUtils.createPage(entities, sambaDomainLdapMapper::mapToDto);
        log.info("{}: Find all samba domains with page request [{}]: Returning page no. {} with {} entries.",
                getCurrentUserName(), pageRequest, dtos.getPageRequest().getPageNumber(), dtos.getEntries().size());
        return dtos;
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SYSTEM','ROLE_READ_ALL_PROFILES')"
            + " or hasPermission(#sambaDomainName, 'SambaDomain', 'read')")
    @Override
    public SambaDomainDto findBySambaDomainName(final String sambaDomainName) {
        log.info("{}: Find samba domain by name [{}] ...", getCurrentUserName(), sambaDomainName);
        SambaDomainLdap entity = sambaDomainLdapDao.findBySambaDomainName(sambaDomainName);
        NotFoundException.validateNotNull(entity, "Samba domain with name [" + sambaDomainName + "] was not found.");
        SambaDomainDto dto = sambaDomainLdapMapper.mapToDto(entity);
        log.info("{}: Find samba domain by name [{}]: DONE!", getCurrentUserName(), sambaDomainName);
        return dto;
    }

    @PreAuthorize("isAuthenticated()")
    @PostAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SYSTEM','ROLE_READ_ALL_PROFILES')"
            + " or hasPermission(#returnObject.sambaDomainName, 'SambaDomain', 'read')")
    @Override
    public SambaDomainDto findBySambaSID(final String sambaSID) {
        log.info("{}: Find samba domain by Samba SID [{}] ...", getCurrentUserName(), sambaSID);
        SambaDomainLdap entity = sambaDomainLdapDao.findBySambaSID(sambaSID);
        NotFoundException.validateNotNull(entity,
                String.format("Samba domain with Samba SID [%s] was not found.", sambaSID));
        SambaDomainDto dto = sambaDomainLdapMapper.mapToDto(entity);
        log.info("{}: Find samba domain by Samba SID [{}]: DONE!", getCurrentUserName(), sambaSID);
        return dto;
    }

    @PreAuthorize("isAuthenticated()")
    @Override
    public boolean existsBySambaDomainName(final String sambaDomainName) {
        BadRequestException.validateNotBlank(sambaDomainName, "Samba domain name must be present.");
        final boolean result = sambaDomainLdapDao.existsBySambaDomainName(sambaDomainName);
        log.info("{}: Exists samba domain by name [{}]? {}", getCurrentUserName(), sambaDomainName, result);
        return result;
    }

    @PreAuthorize("isAuthenticated()")
    @Override
    public boolean existsBySambaSID(final String sambaSID) {
        BadRequestException.validateNotBlank(sambaSID, "Samba SID must be present.");
        final boolean result = sambaDomainLdapDao.existsBySambaSID(sambaSID);
        log.info("{}: Exists samba domain by SID [{}]? {}", getCurrentUserName(), sambaSID, result);
        return result;
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SYSTEM')"
            + " or hasPermission(#sambaDomainName, 'SambaDomain', 'delete')")
    @Override
    public void deleteBySambaDomainName(final String sambaDomainName) {

        log.info("{}: Deleting samba domain by name [{}] ...", getCurrentUserName(), sambaDomainName);
        BadRequestException.validateNotBlank(sambaDomainName, "Samba domain name must be present.");
        sambaDomainLdapDao.deleteBySambaDomainName(sambaDomainName);
        deleteAcls(new ObjectIdentityImpl(SambaDomainDto.TYPE_ID, sambaDomainName), true);
        log.info("{}: Deleting samba domain by name [{}]: DONE!", getCurrentUserName(), sambaDomainName);
    }

    private class Initializer extends RunAsCallbackWithoutResult {

        Initializer() {
            Validate.notNull(sambaDomainLdapDao, "sambaDomainLdapDao must not be null");
            Validate.notNull(sambaDomainLdapMapper, "sambaDomainLdapMapper must not be null");
            Validate.notNull(sambaDomainProperties, "sambaDomainProperties must not be null");
        }

        @Override
        public void run() { // NOSONAR
            boolean defaultSambaDomainExists = false;
            Page<SambaDomainDto> domains = findAll(new PageRequestDto());
            for (final SambaDomainDto domain : domains.getEntries()) {

                if (!defaultSambaDomainExists
                        && domain.getSambaDomainName().equals(sambaDomainProperties.getDefaultSambaDomain()
                        .getSambaDomainName())) {

                    log.info("Default samba domain [{}] exists.", domain.getSambaDomainName());
                    defaultSambaDomainExists = true;
                    sambaDomainProperties.setDefaultSambaDomain(domain);
                }

                ObjectIdentity objectIdentity = getObjectIdentityRetrievalStrategy().getObjectIdentity(domain);
                try {
                    getAclService().readAclById(objectIdentity);
                } catch (org.springframework.security.acls.model.NotFoundException e) { // NOSONAR
                    log.info("No ACL found for samba domain [{}] - creating default ACL.", domain.getSambaDomainName());
                    initAcl(objectIdentity);
                }
            }

            if (!defaultSambaDomainExists
                    && sambaDomainProperties.isCreateDefaultSambaDomain()
                    && StringUtils.isNotBlank(sambaDomainProperties.getDefaultSambaDomain().getSambaDomainName())
                    && StringUtils.isNotBlank(sambaDomainProperties.getDefaultSambaDomain().getSambaSID())) {

                create(sambaDomainProperties.getDefaultSambaDomain());

            } else if (!defaultSambaDomainExists && domains.getEntries().size() == 1) {

                log.info("Setting default samba domain to " + domains.getEntries().get(0));
                sambaDomainProperties.setDefaultSambaDomain(domains.getEntries().get(0));

            } else if (!defaultSambaDomainExists) {

                if (StringUtils.isBlank(sambaDomainProperties.getDefaultSambaDomain().getSambaDomainName())) {
                    sambaDomainProperties.getDefaultSambaDomain().setSambaDomainName("SHARES");
                }
                if (StringUtils.isBlank(sambaDomainProperties.getDefaultSambaDomain().getSambaSID())) {
                    sambaDomainProperties.getDefaultSambaDomain()
                            .setSambaSID("S-1-5-21-0000000000-0000000000-000000000");
                }
                if (sambaDomainProperties.getDefaultSambaDomain().getGidNumber() == null) {
                    sambaDomainProperties.getDefaultSambaDomain().setGidNumber(System.currentTimeMillis() / 1000L);
                }
                if (sambaDomainProperties.getDefaultSambaDomain().getUidNumber() == null) {
                    sambaDomainProperties.getDefaultSambaDomain().setUidNumber(System.currentTimeMillis() / 1000L);
                }
                if (sambaDomainProperties.getDefaultSambaDomain().getSambaAlgorithmicRidBase() == null) {
                    sambaDomainProperties.getDefaultSambaDomain().setSambaAlgorithmicRidBase(1000L);
                }
                log.warn("No default samba domain exists! Using temporary one: {}",
                        sambaDomainProperties.getDefaultSambaDomain());
            }
        }
    }
}
