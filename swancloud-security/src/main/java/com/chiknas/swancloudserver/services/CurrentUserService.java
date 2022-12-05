package com.chiknas.swancloudserver.services;

import com.chiknas.swancloudserver.dto.response.QRSyncResponse;
import com.chiknas.swancloudserver.entities.RefreshToken;
import com.chiknas.swancloudserver.entities.User;
import com.chiknas.swancloudserver.repositories.UserRepository;
import com.chiknas.swancloudserver.security.CurrentUser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class CurrentUserService implements CurrentUser {

    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;

    @Autowired
    public CurrentUserService(UserRepository userRepository, RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.refreshTokenService = refreshTokenService;
    }

    @Override
    public Optional<LocalDateTime> getLastUploadedFileDate() {
        return getCurrentUser().map(User::getLastUploadedFileDate);
    }

    @Override
    public void setLastUploadedFileDate(LocalDateTime localDate) {
        getCurrentUser().ifPresent(user -> {
            if (user.getLastUploadedFileDate() == null || localDate.isAfter(user.getLastUploadedFileDate())) {
                user.setLastUploadedFileDate(localDate);
                userRepository.save(user);
            }
        });
    }

    @Override
    public Optional<String> getSyncUserQR() {
        return getCurrentUser().flatMap(user -> {
            String baseServerUri = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);
            QRSyncResponse qrSyncResponse = QRSyncResponse.builder()
                    .email(user.getEmail())
                    .baseServerUrl(baseServerUri)
                    .refreshToken(refreshToken.getToken())
                    .expiryTime(refreshToken.getExpiryDate().getEpochSecond())
                    .build();
            try {
                String serializedQRSyncResponse = new ObjectMapper().writeValueAsString(qrSyncResponse);
                return QRCodeGenerator.getQRCodeImage(serializedQRSyncResponse, 250, 250);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private Optional<User> getCurrentUser() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(authentication -> (User) authentication.getPrincipal());
    }
}
