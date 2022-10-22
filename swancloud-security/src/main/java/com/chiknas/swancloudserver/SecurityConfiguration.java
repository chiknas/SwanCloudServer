package com.chiknas.swancloudserver;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

import java.util.List;

@Configuration
@EnableWebSecurity
@Profile("production")
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Value("#{${security.api.keys}}")
    private List<String> knownApiKeys;

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.
                antMatcher("/**").
                csrf().disable().
                cors().and().
                sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).
                and().addFilter(new APIKeyAuthFilter(knownApiKeys)).authorizeRequests().anyRequest().authenticated();
    }
}
