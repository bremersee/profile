/*
 * Copyright 2016 the original author or authors.
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

import org.bremersee.profile.model.SambaSettingsDto;

/**
 * @author Christian Bremer
 */
public interface SambaSettingsService {

    void applySambaSettingsToProfile(String userName, SambaSettingsDto sambaSettings);

    void updateSambaSettingsToProfile(String userName, SambaSettingsDto sambaSettings);

    SambaSettingsDto findSambaSettingsByUid(String userName);

    boolean sambaSettingsExistsByUid(String userName);

    void removeSambaSettingsFromProfile(String userName);

}
