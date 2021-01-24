package com.chiknas.swancloudserver.repositories;

import com.chiknas.swancloudserver.entities.FileMetadataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadataEntity, UUID> {
    Optional<FileMetadataEntity> findByFileName(String fileName);
}
