package com.chiknas.swancloudserver.services;


import com.chiknas.swancloudserver.entities.RefreshToken;
import com.chiknas.swancloudserver.entities.User;
import com.chiknas.swancloudserver.repositories.RefreshTokenRepository;
import com.chiknas.swancloudserver.repositories.UserRepository;
import com.chiknas.swancloudserver.repositories.specifications.RefreshTokenSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {
    @Value("${security.jwt.refresh-token-web.expire-length}")
    private Long refreshTokenWebMillis;

    @Value("${security.jwt.refresh-token-api.expire-length}")
    private Long refreshTokenApiMillis;

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

    public RefreshToken createRefreshTokenWeb(User user) {
        return createRefreshToken(user, refreshTokenWebMillis);
    }

    public RefreshToken createRefreshTokenApi(User user) {
        return createRefreshToken(user, refreshTokenApiMillis);
    }

    private RefreshToken createRefreshToken(User user, Long ttl) {
        RefreshToken refreshToken = new RefreshToken();

        refreshToken.setUser(user);
        refreshToken.setExpiryDate(Instant.now().plusMillis(ttl));
        refreshToken.setToken(UUID.randomUUID().toString());

        return refreshTokenRepository.save(refreshToken);
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
        return userRepository.findById(userId).map(refreshTokenRepository::deleteByUser).orElse(0);
    }

    // Delete unused tokens once a day
    @Scheduled(fixedRate = 86400000)
    public void cleanupRefreshTokens() {
        refreshTokenRepository.deleteAll(refreshTokenRepository.findAll(RefreshTokenSpecification.isExpired()));
    }
}
