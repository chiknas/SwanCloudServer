package com.chiknas.swancloudserver.dto;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Data transfer object for file metadata
 *
 * @author nkukn
 * @since 2/23/2021
 */
public interface FileMetadataDTO {

    Integer getId();

    LocalDateTime getCreatedDate();

    File getFile();

    String getFileMimeType();

    Optional<byte[]> getThumbnail();
}
