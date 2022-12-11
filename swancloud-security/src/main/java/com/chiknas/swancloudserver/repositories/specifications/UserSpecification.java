package com.chiknas.swancloudserver.repositories.specifications;

import com.chiknas.swancloudserver.entities.User;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecification {
    private UserSpecification() {
        // Static helper methods to generate specifications. don't init.
    }

    /**
     * Returns the user with the specified email.
     */
    public static Specification<User> withEmail(String email) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("email"), email);
    }
}
