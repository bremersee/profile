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

package org.bremersee.profile;

import org.bremersee.common.spring.autoconfigure.JaxbContextPathsProvider;
import org.bremersee.profile.model.ObjectFactory;
import org.springframework.context.annotation.Configuration;

/**
 * @author Christian Bremer
 */
@Configuration
public class JaxbConfig implements JaxbContextPathsProvider {

    @Override
    public String[] getContextPaths() {
        return new String[]{
                ObjectFactory.class.getPackage().getName()
        };
    }

}
