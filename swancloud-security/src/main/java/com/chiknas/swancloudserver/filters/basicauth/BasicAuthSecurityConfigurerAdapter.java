package com.chiknas.swancloudserver.filters.basicauth;

import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configures basic auth flow functionality.
 */
public class BasicAuthSecurityConfigurerAdapter extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {

    @Override
    public void configure(HttpSecurity http) {
        // Reset expired password filter
        PasswordExpirationFilter passwordExpirationFilter = new PasswordExpirationFilter();
        http.addFilterAfter(passwordExpirationFilter, UsernamePasswordAuthenticationFilter.class);
    }
}
