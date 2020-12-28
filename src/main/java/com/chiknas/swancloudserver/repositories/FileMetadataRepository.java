package com.chiknas.swancloudserver.repositories;

import java.util.UUID;

import com.chiknas.swancloudserver.entities.FileMetadataEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadataEntity, UUID> {
    public FileMetadataEntity findByFileName(String fileName);
}
