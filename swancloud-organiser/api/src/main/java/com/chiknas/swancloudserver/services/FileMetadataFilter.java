package com.chiknas.swancloudserver.services;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class FileMetadataFilter {

    /**
     * Uncategorized files are the ones that the system could not determine their date.
     * They have a date of 1970-01-01 by default.
     */
    private Boolean uncategorized;

    /**
     * Returns files that were created before the specified date.
     * For example a picture taken on 2022 won't be return if we filter for 2021.
     */
    private LocalDateTime beforeDate;
}
