package com.chiknas.swancloudserver;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Provides the necessary beans for the system to work when production mode is off. This way the controllers and other services
 * in here can access these mock beans and start up properly.
 */
@Configuration
@Profile("!production")
public class DevSecurityConfiguration {

    @Bean
    public AuthenticationManager authenticationManager() {
        return authentication -> null;
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
