package com.chiknas.swancloudserver.repositories.specifications;

import com.chiknas.swancloudserver.entities.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserSpecificationTest {

    @Mock
    private Root<User> root;

    @Mock
    private CriteriaQuery<User> query;

    @Mock
    private CriteriaBuilder criteriaBuilder;

    @Test
    void withEmails() {
        // Given user with email spec
        Specification<User> withEmail = UserSpecification.withEmail("user@gmail.com");

        // When the predicate is created
        Path<Object> emailPath = mock(Path.class);
        when(root.get(eq("email"))).thenReturn(emailPath);
        withEmail.toPredicate(root, query, criteriaBuilder);

        // Then the user email should be in the specified list of emails
        verify(criteriaBuilder, times(1)).equal(eq(emailPath), eq("user@gmail.com"));
    }
}