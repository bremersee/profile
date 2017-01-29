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
import org.bremersee.profile.AbstractComponentImpl;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Sid;

/**
 * @author Christian Bremer
 */
public abstract class AbstractServiceImpl extends AbstractComponentImpl {

    MutableAcl initAcl(Object entity) {
        return initAcl(entity, null, null, false);
    }

    MutableAcl initAcl(Object entity, String owner) {
        return initAcl(entity, owner, null, false);
    }

    @SuppressWarnings("WeakerAccess")
    MutableAcl initAcl(Object entity, String owner, Acl parent, boolean entriesInheriting) {
        Validate.notNull(entity, "Entity must not be null.");

        final ObjectIdentity objectIdentity;
        if (entity instanceof ObjectIdentity) {
            objectIdentity = (ObjectIdentity) entity;
        } else {
            objectIdentity = getObjectIdentityRetrievalStrategy().getObjectIdentity(entity);
        }
        MutableAcl acl = getAclService().createAcl(objectIdentity);

        final Sid ownerSid;
        if (StringUtils.isBlank(owner) && parent != null) {
            ownerSid = parent.getOwner();
        } else {
            ownerSid = new PrincipalSid(StringUtils.isBlank(owner) ? getSystemName() : owner);
        }
        acl.setOwner(ownerSid);

        if (parent != null) {
            acl.setParent(parent);
            acl.setEntriesInheriting(entriesInheriting);
        }

        if (!entriesInheriting) {
            for (final String roleName : getAdminAndSystemRoleNames()) {
                final GrantedAuthoritySid roleSid = new GrantedAuthoritySid(roleName);
                acl.insertAce(acl.getEntries().size(), BasePermission.ADMINISTRATION, roleSid, true);
                acl.insertAce(acl.getEntries().size(), BasePermission.CREATE, roleSid, true);
                acl.insertAce(acl.getEntries().size(), BasePermission.DELETE, roleSid, true);
                acl.insertAce(acl.getEntries().size(), BasePermission.READ, roleSid, true);
                acl.insertAce(acl.getEntries().size(), BasePermission.WRITE, roleSid, true);
            }
        }

        return getAclService().updateAcl(acl);
    }

    void deleteAcls(Object entity, boolean deleteChildren) {
        Validate.notNull(entity, "Entity must not be null.");
        if (entity instanceof ObjectIdentity) {
            getAclService().deleteAcl((ObjectIdentity) entity, deleteChildren);
        } else {
            getAclService().deleteAcl(getObjectIdentityRetrievalStrategy().getObjectIdentity(entity), deleteChildren);
        }
    }

}
