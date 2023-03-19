package com.chiknas.swancloudserver.services;

import com.chiknas.swancloudserver.dto.StreamPartialContentDTO;
import org.apache.commons.io.FilenameUtils;
import org.springframework.boot.web.server.MimeMappings;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.file.Files;

/**
 * Service responsible to create all the resources for an HTTP File streaming service.
 * Breaking down lard
 */
@Service
public class FileStreamingService {

    // Size of the maximum chunks to serve each file, in bytes.
    protected static final int BUFFER_SIZE = 4200000;


    /**
     * Generates the data required to response to a partial content http request for the specified file.
     * This will return only a part of the file in byte[] form and further requests will be required
     * to get all the content of the file.
     */
    public StreamPartialContentDTO getStreamPartialContent(File file, HttpRange httpRange) {

        byte[] fileChunk = getFileChunk(file, (int) httpRange.getRangeStart(file.length()));

        HttpHeaders httpHeaders = getHttpHeaders(file, httpRange);

        return StreamPartialContentDTO.builder()
                .chunkData(fileChunk)
                .contentLength(fileChunk.length - 1)
                .httpHeaders(httpHeaders)
                .build();
    }


    /**
     * Reads up to {@link FileStreamingService#BUFFER_SIZE} the file's bytes on the specified position of byte.
     * If files has 10 bytes, and position is 5 then this will return the last 5 bytes.
     * This allows us to read only parts of a file instead of loading the whole thing in memory.
     */
    private byte[] getFileChunk(File file, int position) {
        try (RandomAccessFile read = new RandomAccessFile(file, "r")) {

            read.seek(position);

            byte[] bytes = new byte[BUFFER_SIZE];
            read.read(bytes);

            return bytes;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Parses the string to a {@link HttpRange} object.
     * On failure to parse the string a default range is created:
     * Range: 0-{@link FileStreamingService#BUFFER_SIZE}
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Range">Range HTTP</a>
     */
    public HttpRange parseFirstRangeHeader(String range) {
        return HttpRange.parseRanges(range).stream().findFirst().orElse(HttpRange.createByteRange(0, BUFFER_SIZE));
    }


    /**
     * Required Http headers for a stream endpoint for the specified file.
     * To be used with: {@link HttpStatus#PARTIAL_CONTENT}
     */
    private static HttpHeaders getHttpHeaders(File file, HttpRange httpRange) {
        try {
            int size = (int) Files.size(file.toPath());
            int lower = (int) httpRange.getRangeStart(size);
            int upper = (int) httpRange.getRangeEnd(size);

            String extension = FilenameUtils.getExtension(file.getName());
            String contentType = MimeMappings.DEFAULT.get(extension);

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.set(HttpHeaders.ACCEPT_RANGES, "bytes");
            httpHeaders.set(HttpHeaders.CONTENT_TYPE, contentType);
            httpHeaders.set(HttpHeaders.CONTENT_RANGE, String.format("bytes %d-%d/%d", lower, upper - 1, size));
            return httpHeaders;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate streaming http headers for file: " + file.getName(), e);
        }
    }
}
