package com.chiknas.swancloudserver.controllers.dto;

import java.time.LocalDateTime;

public interface FileMetadataApiDTO {

    Integer getId();

    LocalDateTime getCreatedDate();

    String getFileMimeType();

    String getFileName();

}
