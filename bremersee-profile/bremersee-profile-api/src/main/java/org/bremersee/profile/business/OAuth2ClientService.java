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

import org.bremersee.pagebuilder.model.Page;
import org.bremersee.pagebuilder.model.PageRequest;
import org.bremersee.profile.model.OAuth2ClientCreateRequestDto;
import org.bremersee.profile.model.OAuth2ClientDto;

/**
 * @author Christian Bremer
 */
public interface OAuth2ClientService {

    Page<OAuth2ClientDto> findAll(PageRequest pageRequest);

    OAuth2ClientDto create(OAuth2ClientCreateRequestDto client);

    OAuth2ClientDto update(String clientId, OAuth2ClientDto client);

    void resetPassword(String clientId, String newPassword, boolean skipValidation);

    void changePassword(String clientId, String newPassword, String oldPassword);

    OAuth2ClientDto findByClientId(String clientId);

    boolean existsByClientId(String clientId);

    void deleteByClientId(String clientId);

}
