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

import org.bremersee.profile.controller.rest.AbstractRestControllerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.ApiKeyVehicle;
import springfox.documentation.swagger.web.SecurityConfiguration;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Christian Bremer
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {

    public static final String SECURITY_SCHEMA_OAUTH2 = "oauth2";

    public static final String OPENID_SCOPE = "openid";

    public static final String OPENID_SCOPE_DESCR = "Scope 'openid' can access the '/me' resource.";

    public static final String PROFILE_SCOPE = "profile";

    public static final String PROFILE_SCOPE_DESCR = "Scope 'profile' can access the profile API.";

    private final Logger log = LoggerFactory.getLogger(getClass());

    private Environment env;

    @Autowired
    public void setEnv(Environment env) {
        this.env = env;
    }

    private String getUserAuthorizationUri() {
        return env.getProperty("security.oauth2.client.user-authorization-uri", "");
    }

    private String getClientId() {
        return env.getProperty("security.oauth2.client.client-id", "");
    }

    @PostConstruct
    public void init() {
        // @formatter:off
        log.info("\n"
                + "**********************************************************************\n"
                + "*  Swagger Auto Configuration                                        *\n"
                + "**********************************************************************\n"
                + "userAuthorizationUri = " + getUserAuthorizationUri() + "\n"
                + "clientId = " + getClientId() + "\n"
                + "**********************************************************************");
        // @formatter:on
    }

    @Bean
    public Docket swaggerUiDocket() {
        //@formatter:off
        return new Docket(DocumentationType.SWAGGER_2)
                //.groupName("role-group")
                .apiInfo(new ApiInfoBuilder()
                        .title("Profile Application Rest API")
                        .description("An API to manage user profiles.")
                        .version("v1")
                        .build())
                .select()
                .apis(RequestHandlerSelectors.basePackage(AbstractRestControllerImpl.class.getPackage().getName()))
                .paths(PathSelectors.ant("/api/**"))
                .build()
                .securitySchemes(Collections.singletonList(implicitFlow()))
                .securityContexts(Collections.singletonList(securityContext()));
        //@formatter:on
    }

    private AuthorizationScope getOpenidScope() {
        return new AuthorizationScope("openid", "Scope 'openid' can access the '/me' resource.");
    }

    private AuthorizationScope getProfileScope() {
        return new AuthorizationScope("profile", "Scope 'profile' can access the profile API.");
    }

    private AuthorizationScope[] getScopes() {
        return new AuthorizationScope[]{
                getOpenidScope(),
                getProfileScope()
        };
    }

    private OAuth implicitFlow() {

        return new OAuth(
                SECURITY_SCHEMA_OAUTH2,
                Arrays.asList(getScopes()),
                Collections.singletonList(
                        new ImplicitGrant(
                                new LoginEndpoint(getUserAuthorizationUri()),
                                "access_token")
//                        new AuthorizationCodeGrant(
//                                new TokenRequestEndpoint("http://localhost:9000/oauth/authorize", "bremersee", "secret"),
//                                new TokenEndpoint("http://bremersee:secret@localhost:9000/oauth/token", "access_code"))
                ));
    }

    private SecurityContext securityContext() {
        return SecurityContext.builder()
                .securityReferences(defaultAuth())
                .forPaths(PathSelectors.ant("/api/**"))
                .build();
    }

    private List<SecurityReference> defaultAuth() {
        return Collections.singletonList(
                new SecurityReference(SECURITY_SCHEMA_OAUTH2, getScopes()));
    }

    @Bean
    public SecurityConfiguration swaggerUiSecurityConfiguration() {
        String clientId = getClientId();
        String clientSecret = "";
        String realm = "oauth2/client";
        String appName = "";
        String apiKeyValue = "";
        ApiKeyVehicle apiKeyVehicle = ApiKeyVehicle.HEADER;
        String apiKeyName = "";
        String scopeSeparator = " ";
        return new SecurityConfiguration(
                clientId,
                clientSecret,
                realm,
                appName,
                apiKeyValue,
                apiKeyVehicle,
                apiKeyName,
                scopeSeparator);
    }

}
