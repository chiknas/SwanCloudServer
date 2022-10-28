package com.chiknas.swancloudserver.repositories;

import com.chiknas.swancloudserver.entities.FileMetadataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadataEntity, Integer>, JpaSpecificationExecutor<FileMetadataEntity> {
    Optional<FileMetadataEntity> findByFileName(String fileName);
}
