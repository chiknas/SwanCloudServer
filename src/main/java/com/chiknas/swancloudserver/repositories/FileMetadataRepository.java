package com.chiknas.swancloudserver.repositories;

import com.chiknas.swancloudserver.entities.FileMetadataEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadataEntity, Integer> {
    Optional<FileMetadataEntity> findByFileName(String fileName);

    List<FileMetadataEntity> findAllByThumbnailNull(Sort sort);

    @Query("SELECT fm FROM FileMetadataEntity fm WHERE fm.createdDate <= ?2 AND (fm.id <= ?1 OR fm.createdDate <= ?2) " +
            "ORDER BY fm.createdDate DESC, fm.id DESC")
    List<FileMetadataEntity> findAllAfterCursor(Pageable pageable, int cursorId, LocalDate cursorDate);

    @Query("SELECT fm FROM FileMetadataEntity fm WHERE fm.createdDate='1970-01-01' AND fm.id <= ?1 " +
            "ORDER BY fm.createdDate DESC, fm.id DESC")
    List<FileMetadataEntity> findAllUncategorizedAfterCursor(Pageable pageable, int cursorId);
}
