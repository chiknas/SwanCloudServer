package com.chiknas.swancloudserver.services;

import com.chiknas.swancloudserver.dto.FileMetadataDTO;
import org.springframework.lang.Nullable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

/**
 * Service responsible to handle/manipulate files in the system.
 */
public interface FileService {

    /**
     * Saves a new file in the system.
     */
    Optional<FileMetadataDTO> storeFile(MultipartFile file);

    /**
     * Returns details on the files that exist currently in the system.
     */
    List<FileMetadataDTO> findAllFilesMetadata(int limit, int offset, @Nullable FileMetadataFilter filter);

    /**
     * Returns file metadata of the file with the specified id.
     */
    Optional<FileMetadataDTO> getFileById(Integer id);
}
