package com.chiknas.swancloudserver.services;


import com.chiknas.swancloudserver.entities.RefreshToken;
import com.chiknas.swancloudserver.repositories.RefreshTokenRepository;
import com.chiknas.swancloudserver.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {
    @Value("${security.jwt.refresh-token.expire-length}")
    private Long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;

    private final UserRepository userRepository;

    @Autowired
    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, UserRepository userRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken createRefreshToken(Long userId) {
        return userRepository.findById(userId).map(user -> {
                    RefreshToken refreshToken = new RefreshToken();

                    refreshToken.setUser(userRepository.findById(userId).get());
                    refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
                    refreshToken.setToken(UUID.randomUUID().toString());

                    return refreshTokenRepository.save(refreshToken);
                })
                .orElseThrow(() -> new RuntimeException("User with id: " + userId + " was not found!"));
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new SecurityException("Refresh token was expired. Please make a new signin request");
        }

        return token;
    }

    @Transactional
    public int deleteByUserId(Long userId) {
        return refreshTokenRepository.deleteByUser(userRepository.findById(userId).get());
    }
}
