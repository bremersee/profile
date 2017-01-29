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

package org.bremersee.profile;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.bremersee.common.security.acls.model.ObjectIdentityUtils;
import org.bremersee.common.security.core.context.RunAsCallback;
import org.bremersee.common.security.core.context.RunAsCallbackWithoutResult;
import org.bremersee.common.security.core.context.RunAsUtil;
import org.bremersee.profile.model.RoleDto;
import org.bremersee.profile.model.UserProfileDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.ObjectIdentityGenerator;
import org.springframework.security.acls.model.ObjectIdentityRetrievalStrategy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.PlatformTransactionManager;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Christian Bremer
 */
public abstract class AbstractComponentImpl {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    private PlatformTransactionManager transactionManager;

    private MutableAclService aclService;

    private ObjectIdentityRetrievalStrategy objectIdentityRetrievalStrategy;

    private ObjectIdentityGenerator objectIdentityGenerator;

    private PermissionEvaluator permissionEvaluator;

    private AbstractComponentProperties commmonProperties = new AbstractComponentProperties();

    private Set<String> adminRoleNames;

    private Set<String> systemRoleNames;

    private Set<String> adminAndSystemRoleNames;

    @PostConstruct
    public final void init() {
        log.info("Initializing " + getClass().getSimpleName() + " ...");

        final Set<String> adminRoles = new HashSet<>();
        adminRoles.add(RoleDto.ADMIN_ROLE_NAME);
        adminRoles.add(RoleDto.ACL_ADMIN_ROLE_NAME);
        adminRoles.addAll(commmonProperties.getAdminRoleNames());
        adminRoleNames = Collections.unmodifiableSet(adminRoles);

        final Set<String> systemRoles = new HashSet<>();
        systemRoles.add(RoleDto.SYSTEM_ROLE_NAME);
        systemRoles.add(RoleDto.ACL_ADMIN_ROLE_NAME);
        systemRoles.addAll(commmonProperties.getSystemRoleNames());
        systemRoleNames = Collections.unmodifiableSet(systemRoles);

        final Set<String> adminAndSystemRoles = new HashSet<>();
        adminAndSystemRoles.addAll(adminRoles);
        adminAndSystemRoles.addAll(systemRoles);
        adminAndSystemRoleNames = Collections.unmodifiableSet(adminAndSystemRoles);


        doInit();
        log.info(getClass().getSimpleName() + " successfully initialized.");
    }

    protected abstract void doInit();

    @SuppressWarnings("unused")
    protected PlatformTransactionManager getTransactionManager() {
        return transactionManager;
    }

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    protected MutableAclService getAclService() {
        return aclService;
    }

    @Autowired
    public void setAclService(MutableAclService aclService) {
        this.aclService = aclService;
    }

    protected ObjectIdentityRetrievalStrategy getObjectIdentityRetrievalStrategy() {
        return objectIdentityRetrievalStrategy;
    }

    @Autowired
    public void setObjectIdentityRetrievalStrategy(ObjectIdentityRetrievalStrategy objectIdentityRetrievalStrategy) {
        this.objectIdentityRetrievalStrategy = objectIdentityRetrievalStrategy;
    }

    @SuppressWarnings("unused")
    protected ObjectIdentityGenerator getObjectIdentityGenerator() {
        return objectIdentityGenerator;
    }

    @Autowired
    public void setObjectIdentityGenerator(ObjectIdentityGenerator objectIdentityGenerator) {
        this.objectIdentityGenerator = objectIdentityGenerator;
    }

    @SuppressWarnings("unused")
    protected PermissionEvaluator getPermissionEvaluator() {
        return permissionEvaluator;
    }

    @Autowired
    public void setPermissionEvaluator(PermissionEvaluator permissionEvaluator) {
        this.permissionEvaluator = permissionEvaluator;
    }

    @Autowired(required = false)
    public void setCommmonProperties(AbstractComponentProperties commmonProperties) {
        if (commmonProperties != null) {
            this.commmonProperties = commmonProperties;
        }
    }

    protected String getCurrentUserName() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth == null ? null : auth.getName();
    }

    protected Collection<? extends GrantedAuthority> getCurrentUserRoles() { // NOSONAR
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth == null ? Collections.emptyList() : auth.getAuthorities();
    }

    @SuppressWarnings("unused")
    protected Set<String> getCurrentUserRoleNames() {
        Collection<? extends GrantedAuthority> grantedAuthorities = getCurrentUserRoles();
        return grantedAuthorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
    }

    protected UserProfileDto getAdminProfile() {
        return commmonProperties.getAdminProfile();
    }

    protected String getAdminName() {
        return getAdminProfile().getUid();
    }

    protected String getSystemName() {
        return commmonProperties.getSystemName();
    }

    protected Set<String> getAdminAndSystemRoleNames() {
        return adminAndSystemRoleNames;
    }

    private boolean isCurrentUserAdmin() {
        for (GrantedAuthority authority : getCurrentUserRoles()) {
            if (adminRoleNames.contains(authority.getAuthority())) {
                return true;
            }
        }
        return false;
    }

    private boolean isCurrentUserSystem() {
        for (GrantedAuthority authority : getCurrentUserRoles()) {
            if (systemRoleNames.contains(authority.getAuthority())) {
                return true;
            }
        }
        return false;
    }

    protected boolean isCurrentUserAdminOrSystem() {
        return isCurrentUserAdmin() || isCurrentUserSystem();
    }

    protected <T> T runAsSystem(final RunAsCallback<T> callback) {
        if (isCurrentUserAdminOrSystem()) {
            return callback.execute();
        } else {
            return RunAsUtil.runAs(
                    getSystemName(),
                    getAdminAndSystemRoleNames().toArray(new String[getAdminAndSystemRoleNames().size()]),
                    callback);
        }
    }

    protected void runAsSystemWithoutResult(final RunAsCallbackWithoutResult callback) {
        runAsSystem(callback);
    }

    protected ObjectIdentity createObjectIdentityWithAttribute(final Serializable id, final String type, final String attribute) {
        Validate.notNull(id, "ID must not be null.");
        Validate.notBlank(type, "Type must not be null or blank.");
        if (StringUtils.isBlank(attribute)) {
            return objectIdentityGenerator.createObjectIdentity(id, ObjectIdentityUtils.getType(type));
        }
        return objectIdentityGenerator.createObjectIdentity(id,
                ObjectIdentityUtils.createTypeWithAttribute(type, attribute));
    }

}
