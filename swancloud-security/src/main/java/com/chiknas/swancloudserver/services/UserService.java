package com.chiknas.swancloudserver.services;

import com.chiknas.swancloudserver.dto.request.PasswordResetRequest;
import com.chiknas.swancloudserver.entities.User;
import com.chiknas.swancloudserver.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional
@Service("userDetailsService")
public class UserService implements UserDetailsService {

    private final UserRepository usersRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository usersRepository, PasswordEncoder passwordEncoder) {
        this.usersRepository = usersRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    @Override
    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        return usersRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User " + username + " not found."));
    }

    public Optional<User> changePassword(User user, PasswordResetRequest request) throws UsernameNotFoundException {
        String newPassword = request.getNewPassword();
        String oldPassword = request.getOldPassword();

        if (oldPassword.equals(newPassword)) {
            return Optional.empty();
        }
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            return Optional.empty();
        }

        // Kick user out, so he can log in with the new password
        SecurityContextHolder.getContext().setAuthentication(null);

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordExpired(false);
        return Optional.of(usersRepository.save(user));
    }

}
