package com.chiknas.swancloudserver.services;

import com.chiknas.swancloudserver.entities.User;
import com.chiknas.swancloudserver.repositories.UserRepository;
import com.chiknas.swancloudserver.security.CurrentUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class CurrentUserService implements CurrentUser {

    private final UserRepository userRepository;

    @Autowired
    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<LocalDate> getLastUploadedFileDate() {
        return getCurrentUser().map(User::getLastUploadedFileDate);
    }

    @Override
    public void setLastUploadedFileDate(LocalDate localDate) {
        getCurrentUser().ifPresent(user -> {
            if (user.getLastUploadedFileDate() == null || localDate.isAfter(user.getLastUploadedFileDate())) {
                user.setLastUploadedFileDate(localDate);
                userRepository.save(user);
            }
        });
    }

    private Optional<User> getCurrentUser() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(authentication -> (User) authentication.getPrincipal());
    }
}
