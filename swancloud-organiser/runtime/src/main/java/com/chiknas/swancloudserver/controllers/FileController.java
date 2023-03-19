package com.chiknas.swancloudserver.controllers;

import com.chiknas.swancloudserver.dto.FileMetadataDTO;
import com.chiknas.swancloudserver.dto.StreamPartialContentDTO;
import com.chiknas.swancloudserver.services.FileService;
import com.chiknas.swancloudserver.services.FileStreamingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;

/**
 * Controller to host endpoints related to direct operations with the files on the filesystem.
 */
@RestController
@RequestMapping("api")
public class FileController {

    private final FileService fileService;
    private final FileStreamingService fileStreamingService;

    @Autowired
    public FileController(FileService fileService, FileStreamingService fileStreamingService) {
        this.fileService = fileService;
        this.fileStreamingService = fileStreamingService;
    }

    @GetMapping("/files/video/{id}")
    public ResponseEntity<ByteArrayResource> getVideoByName(@RequestHeader(HttpHeaders.RANGE) String range, @PathVariable Integer id) {
        return fileService.getFileById(id)
                .filter(x -> x.getFileMimeType().contains("video"))
                .map(FileMetadataDTO::getFile)
                .map(x -> getByteArrayResourceResponseEntity(range, x))
                .orElseThrow(() -> new RuntimeException("Video with id: " + id + " not found."));
    }

    private ResponseEntity<ByteArrayResource> getByteArrayResourceResponseEntity(String range, File x) {
        HttpRange httpRange = fileStreamingService.parseFirstRangeHeader(range);

        StreamPartialContentDTO chunk = fileStreamingService.getStreamPartialContent(x, httpRange);

        return ResponseEntity
                .status(HttpStatus.PARTIAL_CONTENT)
                .contentLength(chunk.getContentLength())
                .headers(chunk.getHttpHeaders())
                .body(new ByteArrayResource(chunk.getChunkData()));
    }
}
