package com.chiknas.swancloudserver.repositories.specifications;

import com.chiknas.swancloudserver.entities.FileMetadataEntity;
import org.springframework.data.jpa.domain.Specification;

public class FileMetadataSpecification {

    private FileMetadataSpecification() {
        // Static helper methods to generate specifications. dont init.
    }

    /**
     * Uncategorized files have the lowest date possible (1970-01-01) or in other words
     * timestamp of the first epoch second.
     */
    public static Specification<FileMetadataEntity> isUncategorized() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("createdDate"), "1970-01-01");
    }
}
