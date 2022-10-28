package com.chiknas.swancloudserver.services;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileMetadataFilter {

    /**
     * Uncategorized files are the ones that the system could not determine their date.
     * They have a date of 1970-01-01 by default.
     */
    boolean uncategorized;
}
