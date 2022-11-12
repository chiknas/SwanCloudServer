package com.chiknas.swancloudserver;

import com.chiknas.swancloudserver.entities.User;
import com.chiknas.swancloudserver.repositories.RefreshTokenRepository;
import com.chiknas.swancloudserver.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class DataInitializer implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    UserRepository users;
    @Autowired
    RefreshTokenRepository refreshTokenRepository;
    @Autowired
    PasswordEncoder passwordEncoder;


    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        refreshTokenRepository.deleteAll();
        users.deleteAll();
        this.users.save(User.builder()
                .username("user@gmail.com")
                .password(this.passwordEncoder.encode("password"))
                .roles(Arrays.asList("ROLE_USER"))
                .build()
        );
        this.users.save(User.builder()
                .username("admin")
                .password(this.passwordEncoder.encode("password"))
                .roles(Arrays.asList("ROLE_USER", "ROLE_ADMIN"))
                .build()
        );
    }
}
