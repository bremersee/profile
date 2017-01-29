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

import lombok.Data;
import org.bremersee.profile.model.SambaDomainDto;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Christian Bremer
 */
@ConfigurationProperties("profile.business.samba-domain")
@Data
public class SambaDomainProperties {

    private boolean createDefaultSambaDomain = false;

    private SambaDomainDto defaultSambaDomain;

    public SambaDomainProperties() {
        defaultSambaDomain = new SambaDomainDto();
        defaultSambaDomain.setSambaAlgorithmicRidBase(1000L);
        defaultSambaDomain.setSambaDomainName("SHARES");
        defaultSambaDomain.setSambaForceLogoff(-1);
        defaultSambaDomain.setSambaLockoutDuration(30);
        defaultSambaDomain.setSambaLockoutObservationWindow(30);
        defaultSambaDomain.setSambaLockoutThreshold(0);
        defaultSambaDomain.setSambaLogonToChgPwd(0);
        defaultSambaDomain.setSambaMinPwdAge(0);
        defaultSambaDomain.setSambaMinPwdLength(8);
        defaultSambaDomain.setSambaNextUserRid(1000L);
        defaultSambaDomain.setSambaPwdHistoryLength(0);
        defaultSambaDomain.setSambaRefuseMachinePwdChange(0);
        defaultSambaDomain.setSambaSID("S-1-5-21-0000000000-0000000000-000000000");
        defaultSambaDomain.setGidNumber(10000L);
        defaultSambaDomain.setUidNumber(10000L);
    }

}
