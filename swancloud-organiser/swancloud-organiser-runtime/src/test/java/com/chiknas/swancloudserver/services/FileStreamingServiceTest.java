package com.chiknas.swancloudserver.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpRange;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FileStreamingServiceTest {

    private FileStreamingService fileStreamingService;

    @BeforeEach
    void setUp() {
        fileStreamingService = new FileStreamingService();
    }


    /**
     * Minimal http range header can be parsed to spring's
     * HttpRange object. This header specified the position of the first
     * and the position of the last byte of the file
     * we are interested in.
     */
    @Test
    void parseRangeHeader() {
        // Given a valid single HTTP range header value
        String rangeHeader = "bytes=0-100";

        // Then an http range object starting at 0 position and ending at 100 should be created
        HttpRange httpRange = fileStreamingService.parseFirstRangeHeader(rangeHeader);

        HttpRange expectedHttpRange = HttpRange.createByteRange(0, 100);
        assertEquals(expectedHttpRange, httpRange);
    }

    @Test
    void parseRangeHeaderStart() {
        // Given a range of only the starting position
        String rangeHeader = "bytes=0-";

        // Then an http range object starting at 0 position should be created
        HttpRange httpRange = fileStreamingService.parseFirstRangeHeader(rangeHeader);

        HttpRange expectedHttpRange = HttpRange.createByteRange(0);
        assertEquals(expectedHttpRange, httpRange);
    }

    @Test
    void parseRangeHeaderNull() {
        // Given a null range header a default byte range is created
        HttpRange httpRange = fileStreamingService.parseFirstRangeHeader(null);

        HttpRange expectedHttpRange = HttpRange.createByteRange(0, FileStreamingService.BUFFER_SIZE);
        assertEquals(expectedHttpRange, httpRange);
    }

    @Test
    void parseRangeHeaderMultipleRanges() {
        // Given a valid multi HTTP range header values
        String rangeHeader = "bytes=50-100, 150-200";

        // Then an http range object starting at 50 position and ending at 100 should be created
        // ignoring the rest of the values
        HttpRange httpRange = fileStreamingService.parseFirstRangeHeader(rangeHeader);

        HttpRange expectedHttpRange = HttpRange.createByteRange(50, 100);
        assertEquals(expectedHttpRange, httpRange);
    }


    /**
     * Tests a suffix range header string can be parsed successfully.
     * A suffix header asks for the x amount of bytes at the end of the file.
     */
    @Test
    void parseRangeHeaderSuffixRange() {
        // Given a valid suffix HTTP range header value
        String rangeHeader = "bytes=-200";

        // Then an http suffix range of 200 is created
        HttpRange httpRange = fileStreamingService.parseFirstRangeHeader(rangeHeader);

        HttpRange expectedHttpRange = HttpRange.createSuffixRange(200);
        assertEquals(expectedHttpRange, httpRange);
    }
}