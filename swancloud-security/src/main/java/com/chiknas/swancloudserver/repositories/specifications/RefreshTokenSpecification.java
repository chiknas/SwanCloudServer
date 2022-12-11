package com.chiknas.swancloudserver.repositories.specifications;

import com.chiknas.swancloudserver.entities.RefreshToken;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;

public class RefreshTokenSpecification {
    private RefreshTokenSpecification() {
        // Static helper methods to generate specifications. dont init.
    }

    /**
     * Checks if a refresh token is expired, meaning its expiry date is in the past.
     */
    public static Specification<RefreshToken> isExpired() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.lessThan(root.get("expiry_date"), Instant.now());
    }
}
