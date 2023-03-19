package com.chiknas.swancloudserver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import java.io.File;

/**
 * Data required to generate a {@link HttpStatus#PARTIAL_CONTENT} request.
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class StreamPartialContentDTO {

    private File file;

    private HttpHeaders httpHeaders;

    private byte[] chunkData;

    private int contentLength;

}
