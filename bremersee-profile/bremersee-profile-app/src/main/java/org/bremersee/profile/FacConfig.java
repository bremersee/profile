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

import org.bremersee.fac.FailedAccessCounter;
import org.bremersee.fac.FailedAccessCounterImpl;
import org.bremersee.fac.domain.FailedAccessDao;
import org.bremersee.fac.domain.mongo.FailedAccessMongoDao;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * @author Christian Bremer
 */
@Configuration
@EnableMongoRepositories(basePackages = "org.bremersee.fac.domain.mongo")
public class FacConfig {

    private final Object lock = new Object();

    private FailedAccessCounterImpl fac;

    @Bean
    @ConfigurationProperties(prefix = "profile.business.fac")
    public FailedAccessCounter failedAccessCounter() {
        synchronized (lock) {
            if (fac == null) {
                fac = new FailedAccessCounterImpl();
                fac.setFailedAccessDao(failedAccessDao());
            }
            return fac;
        }
    }

    @Bean
    public FailedAccessDao failedAccessDao() {
        return new FailedAccessMongoDao();
    }

}
