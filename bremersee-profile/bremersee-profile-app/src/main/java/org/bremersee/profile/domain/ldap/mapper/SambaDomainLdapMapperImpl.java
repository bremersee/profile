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

package org.bremersee.profile.domain.ldap.mapper;

import org.bremersee.profile.domain.ldap.entity.SambaDomainLdap;
import org.bremersee.profile.model.SambaDomainDto;
import org.springframework.stereotype.Component;

/**
 * @author Christian Bremer
 */
@Component("sambaDomainLdapMapper")
public class SambaDomainLdapMapperImpl extends AbstractLdapMapperImpl implements SambaDomainLdapMapper {

    @Override
    protected void doInit() {
        // nothing to log
    }

    @Override
    public void mapToDto(SambaDomainLdap source, SambaDomainDto destination) {
        destination.setGidNumber(source.getGidNumber());
        destination.setSambaSID(source.getSambaSID());
        destination.setSambaAlgorithmicRidBase(source.getSambaAlgorithmicRidBase());
        destination.setSambaDomainName(source.getSambaDomainName());
        destination.setSambaForceLogoff(source.getSambaForceLogoff());
        destination.setSambaLockoutDuration(source.getSambaLockoutDuration());
        destination.setSambaLockoutObservationWindow(source.getSambaLockoutObservationWindow());
        destination.setSambaLockoutThreshold(source.getSambaLockoutThreshold());
        destination.setSambaLogonToChgPwd(source.getSambaLogonToChgPwd());
        destination.setSambaMaxPwdAge(source.getSambaMaxPwdAge());
        destination.setSambaMinPwdAge(source.getSambaMinPwdAge());
        destination.setSambaMinPwdLength(source.getSambaMinPwdLength());
        destination.setSambaPwdHistoryLength(source.getSambaPwdHistoryLength());
        destination.setSambaNextUserRid(source.getSambaNextUserRid());
        destination.setSambaRefuseMachinePwdChange(source.getSambaRefuseMachinePwdChange());
        destination.setUidNumber(source.getUidNumber());
    }

    @Override
    public SambaDomainDto mapToDto(SambaDomainLdap source) {
        SambaDomainDto destination = new SambaDomainDto();
        mapToDto(source, destination);
        return destination;
    }

    @Override
    public void updateEntity(SambaDomainDto source, SambaDomainLdap destination) {
        destination.setGidNumber(source.getGidNumber());
        destination.setSambaSID(source.getSambaSID());
        destination.setSambaAlgorithmicRidBase(source.getSambaAlgorithmicRidBase());
        //destination.setSambaDomainName(source.getSambaDomainName()); // NOSONAR
        destination.setSambaForceLogoff(source.getSambaForceLogoff());
        destination.setSambaLockoutDuration(source.getSambaLockoutDuration());
        destination.setSambaLockoutObservationWindow(source.getSambaLockoutObservationWindow());
        destination.setSambaLockoutThreshold(source.getSambaLockoutThreshold());
        destination.setSambaLogonToChgPwd(source.getSambaLogonToChgPwd());
        destination.setSambaMaxPwdAge(source.getSambaMaxPwdAge());
        destination.setSambaMinPwdAge(source.getSambaMinPwdAge());
        destination.setSambaMinPwdLength(source.getSambaMinPwdLength());
        destination.setSambaPwdHistoryLength(source.getSambaPwdHistoryLength());
        destination.setSambaNextUserRid(source.getSambaNextUserRid());
        destination.setSambaRefuseMachinePwdChange(source.getSambaRefuseMachinePwdChange());
        destination.setUidNumber(source.getUidNumber());
    }
}
