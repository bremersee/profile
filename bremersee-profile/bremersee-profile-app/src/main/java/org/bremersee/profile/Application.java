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

package org.bremersee.profile;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.security.KeyPair;

/**
 * @author Christian Bremer
 */
@SpringBootApplication
@EnableMongoRepositories(basePackages = "org.bremersee.profile.domain.mongodb.repository")
@EnableGlobalMethodSecurity(jsr250Enabled = true, prePostEnabled = true, securedEnabled = true)
public class Application extends WebSecurityConfigurerAdapter {

    private AuthenticationManager authenticationManager;

    @Autowired
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.parentAuthenticationManager(authenticationManager);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .formLogin().loginPage("/login").permitAll()
                .and()
                .requestMatcher(new NegatedRequestMatcher(new AntPathRequestMatcher("/api/**")))
                .authorizeRequests()
                .anyRequest().authenticated();
    }

    @Configuration
    public static class WebMvcConfiguration extends WebMvcConfigurerAdapter {

        @Override
        public void addViewControllers(ViewControllerRegistry registry) {

            registry.addViewController("/login").setViewName("login");
            registry.addViewController("/oauth/confirm_access").setViewName("authorize");
        }
    }

    @Configuration
    public static class GlobalAuthenticationConfiguration extends GlobalAuthenticationConfigurerAdapter {

        private PasswordEncoder passwordEncoder;

        private UserDetailsService userDetailsService;

        @Autowired
        public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
            this.passwordEncoder = passwordEncoder;
        }

        @Autowired
        public void setUserDetailsService(UserDetailsService userDetailsService) {
            this.userDetailsService = userDetailsService;
        }

        @Override
        public void init(AuthenticationManagerBuilder auth) throws Exception {
            auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
        }
    }

    @Configuration
    @EnableAuthorizationServer
    @EnableConfigurationProperties({AuthorizationServerProperties.class})
    public static class AuthorizationServerConfiguration extends AuthorizationServerConfigurerAdapter {

        private final Logger log = LoggerFactory.getLogger(getClass());

        private AuthorizationServerProperties authorizationServerProperties = new AuthorizationServerProperties();

        private PasswordEncoder passwordEncoder;

        private AuthenticationManager authenticationManager;

        private ClientDetailsService clientDetailsService;

        @Autowired(required = false)
        public void setAuthorizationServerProperties(AuthorizationServerProperties authorizationServerProperties) {
            this.authorizationServerProperties = authorizationServerProperties;
        }

        @Autowired
        public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
            this.passwordEncoder = passwordEncoder;
        }

        @Autowired
        public void setAuthenticationManager(AuthenticationManager authenticationManager) {
            this.authenticationManager = authenticationManager;
        }

        @Autowired
        @Qualifier("oAuth2ClientDetailsService")
        public void setClientDetailsService(ClientDetailsService clientDetailsService) {
            this.clientDetailsService = clientDetailsService;
        }

        @Bean
        public JwtAccessTokenConverter jwtAccessTokenConverter() {

            DefaultResourceLoader resourceLoader = new DefaultResourceLoader();
            KeyStoreKeyFactory keyStoreFactory = new KeyStoreKeyFactory(
                    resourceLoader.getResource(authorizationServerProperties.getJwtKeyStoreLocation()),
                    authorizationServerProperties.getJwtKeyStorePassword().toCharArray());

            KeyPair keyPair;
            if (StringUtils.isBlank(authorizationServerProperties.getJwtKeyPairPassword())) {
                keyPair = keyStoreFactory.getKeyPair(authorizationServerProperties.getJwtKeyPairAlias());
            } else {
                keyPair = keyStoreFactory.getKeyPair(
                        authorizationServerProperties.getJwtKeyPairAlias(),
                        authorizationServerProperties.getJwtKeyPairPassword().toCharArray());
            }

            JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
            converter.setKeyPair(keyPair);
            return converter;
        }

        @Bean
        public JwtTokenStore jwtTokenStore() {
            JwtTokenStore tokenStore = new JwtTokenStore(jwtAccessTokenConverter());
            if (log.isDebugEnabled()) {
                log.debug("Returning JWT token store: " + tokenStore);
            }
            return tokenStore;
        }

        @Override
        public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
            clients.withClientDetails(clientDetailsService);
        }

        @Override
        public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
            security
                    .tokenKeyAccess(authorizationServerProperties.getTokenKeyAccess())
                    .checkTokenAccess(authorizationServerProperties.getCheckTokenAccess())
                    .realm(authorizationServerProperties.getRealm())
                    .passwordEncoder(passwordEncoder)
            ;
            if (authorizationServerProperties.isAllowFormAuthenticationForClients()) {
                security.allowFormAuthenticationForClients();
            }
            if (authorizationServerProperties.isSslOnly()) {
                security.sslOnly();
            }
        }

        @Override
        public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
            endpoints.authenticationManager(authenticationManager).accessTokenConverter(jwtAccessTokenConverter());
            endpoints.tokenStore(jwtTokenStore());
        }
    }

    @Configuration
    @EnableResourceServer
    public static class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {

        @Override
        public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
            // nothing to configure
        }

        @Override
        public void configure(HttpSecurity http) throws Exception {
            // Working config plain
//            http                                                              // NOSONAR
//                    .antMatcher("/api/**")                                    // NOSONAR
//                    .authorizeRequests().anyRequest().authenticated();        // NOSONAR

            // Working config with options allowed
//            http                                                              // NOSONAR
//                    .antMatcher("/api/**").authorizeRequests()                // NOSONAR
//                    .antMatchers(HttpMethod.OPTIONS).permitAll()              // NOSONAR
//                    .anyRequest().authenticated();                            // NOSONAR

            // test config
            http
                    .antMatcher("/api/**").authorizeRequests()
                    .antMatchers(HttpMethod.OPTIONS).permitAll()
                    .antMatchers(HttpMethod.PUT, "/api/user-registration").permitAll()
                    .antMatchers(HttpMethod.GET, "/api/user-registration/validation/**").permitAll()
                    .anyRequest().authenticated();
        }
    }

}
