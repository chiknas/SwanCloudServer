package com.chiknas.swancloudserver;

import com.chiknas.swancloudserver.entities.User;
import com.chiknas.swancloudserver.repositories.RefreshTokenRepository;
import com.chiknas.swancloudserver.repositories.UserRepository;
import com.chiknas.swancloudserver.repositories.specifications.UserSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SecurityInitializationApplicationListener implements ApplicationListener<ApplicationReadyEvent> {

    @Value("#{'${admin-emails}'.split(',')}")
    private List<String> adminEmails;

    /**
     * Users in the system are baked in. Meaning when users are generated at startup
     * they can not be changed/updated or create new ones. This is the default password
     * for each new user. Access to the system is restricted if the user has not changed this default password.
     */
    public static final String DEFAULT_PASSWORD = "changeme";

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public SecurityInitializationApplicationListener(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        // Cleanup all refresh tokens. All sessions will need to restart.
        refreshTokenRepository.deleteAll();

        // Create accounts for all the specified emails if they don't exist already.
        List<User> admins = adminEmails.stream().filter(email -> !userRepository.exists(UserSpecification.withEmail(email)))
                .map(email -> User.builder()
                        .email(email)
                        .password(this.passwordEncoder.encode(DEFAULT_PASSWORD))
                        .roles(List.of("ROLE_ADMIN"))
                        .build()
                ).collect(Collectors.toList());
        userRepository.saveAll(admins);
    }
}
