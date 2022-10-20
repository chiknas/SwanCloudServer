package com.chiknas.swancloudserver.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * DTO to be used to update a file's creation date.
 *
 * @author nkukn
 * @since 1/24/2021
 */
@Getter
@Setter
public class SetFileDateDTO {

    // the metadata id of the file to update
    private Integer fileId;

    // the new creation date for the specified file
    private LocalDate creationDate;
}
