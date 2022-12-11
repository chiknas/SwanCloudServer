package com.chiknas.swancloudserver.repositories.specifications;

import com.chiknas.swancloudserver.entities.FileMetadataEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FileMetadataSpecificationTest {

    @Mock
    private Root<FileMetadataEntity> root;

    @Mock
    private CriteriaQuery<FileMetadataEntity> query;

    @Mock
    private CriteriaBuilder criteriaBuilder;

    @Test
    public void isUncategorized() {
        // Given uncategorized spec
        Specification<FileMetadataEntity> uncategorized = FileMetadataSpecification.forUncategorized(true);

        // When the predicate is created
        uncategorized.toPredicate(root, query, criteriaBuilder);

        // Then the created date is checked that is the first epoch date
        Path<Object> createdDate = root.get("createdDate");
        verify(criteriaBuilder, times(1)).equal(eq(createdDate), eq(LocalDate.parse("1970-01-01").atStartOfDay()));
    }
}