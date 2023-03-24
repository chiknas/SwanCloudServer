package com.chiknas.swancloudserver.repositories.specifications;

import com.chiknas.swancloudserver.entities.FileMetadataEntity;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class FileMetadataSpecification {

    private FileMetadataSpecification() {
        // Static helper methods to generate specifications. dont init.
    }

    /**
     * Uncategorized files have the lowest date possible (1970-01-01) or in other words
     * timestamp of the first epoch second.
     */
    public static Specification<FileMetadataEntity> forUncategorized(Boolean uncategorized) {
        if (uncategorized == null) {
            return Specification.where(null);
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("createdDate"), LocalDate.parse("1970-01-01").atStartOfDay());
    }

    /**
     * Created date of the file must be on or before the specified date.
     */
    public static Specification<FileMetadataEntity> forBeforeDate(LocalDateTime beforeDate) {
        if (beforeDate == null) {
            return Specification.where(null);
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.lessThanOrEqualTo(root.get("createdDate"), beforeDate);
    }

    /**
     * Returns all files that have thumbnail associated with them.
     */
    public static Specification<FileMetadataEntity> hasThumbnail() {
        return (root, query, criteriaBuilder) -> root.get("thumbnail").isNotNull();
    }
}
