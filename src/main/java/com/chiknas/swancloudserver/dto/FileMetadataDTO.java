package com.chiknas.swancloudserver.dto;

import java.time.LocalDate;

/**
 * Data transfer object for file metadata
 *
 * @author nkukn
 * @since 2/23/2021
 */
public interface FileMetadataDTO {

    Integer getId();

    String getFileName();

    LocalDate getCreatedDate();
}
