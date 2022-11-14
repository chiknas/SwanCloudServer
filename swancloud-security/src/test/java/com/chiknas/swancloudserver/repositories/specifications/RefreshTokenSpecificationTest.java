package com.chiknas.swancloudserver.repositories.specifications;

import com.chiknas.swancloudserver.entities.RefreshToken;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RefreshTokenSpecificationTest {

    @Mock
    private Root<RefreshToken> root;

    @Mock
    private CriteriaQuery<RefreshToken> query;

    @Mock
    private CriteriaBuilder criteriaBuilder;

    @Test
    void isExpired() {
        // Given uncategorized spec
        Specification<RefreshToken> expired = RefreshTokenSpecification.isExpired();

        // When the predicate is created
        expired.toPredicate(root, query, criteriaBuilder);

        // Then the expiryDate is before the current date
        verify(criteriaBuilder, times(1)).lessThan(eq(root.get("expiry_date")), any(Instant.class));
    }
}