package com.chiknas.swancloudserver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@Profile("production")
public class SecurityConfiguration {

    private final SecurityAdminConfigurationProperties securityAdminConfigurationProperties;

    @Autowired
    public SecurityConfiguration(SecurityAdminConfigurationProperties securityAdminConfigurationProperties) {
        this.securityAdminConfigurationProperties = securityAdminConfigurationProperties;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests((authorizeHttpRequestsCustomizer) -> {
                            try {
                                authorizeHttpRequestsCustomizer
                                        .antMatchers("/login", "/img/**", "/css/**", "/oauth/**", "/access-denied")
                                        .permitAll()
                                        .anyRequest().authenticated()
                                        .and()
                                        .oauth2Login()
                                        .loginPage("/login")
                                        .and()
                                        .addFilterAfter(new AuthorizedUsersFilter(securityAdminConfigurationProperties.getAccounts()), OAuth2LoginAuthenticationFilter.class)
                                        .exceptionHandling()
                                        .accessDeniedPage("/access-denied");
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                )
                .httpBasic(withDefaults())
                .build();
    }
}
