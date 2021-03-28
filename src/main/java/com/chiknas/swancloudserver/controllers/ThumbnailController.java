package com.chiknas.swancloudserver.controllers;

import com.chiknas.swancloudserver.entities.FileMetadataEntity;
import com.chiknas.swancloudserver.services.FileService;
import com.chiknas.swancloudserver.services.ThumbnailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * @author nkukn
 * @since 3/28/2021
 */
@RestController
@RequestMapping("api")
public class ThumbnailController {

    private final ThumbnailService thumbnailService;
    private final FileService fileService;

    @Autowired
    public ThumbnailController(ThumbnailService thumbnailService, FileService fileService) {
        this.thumbnailService = thumbnailService;
        this.fileService = fileService;
    }

    @GetMapping("/files/thumbnail/{id}")
    public ResponseEntity<byte[]> getFileThumbnail(@PathVariable Integer id) {
        HttpHeaders headers = new HttpHeaders();

        final Optional<String> fileName = fileService.findFileMetadataById(id).map(FileMetadataEntity::getFileName);

        if (fileName.isEmpty()) {
            return new ResponseEntity<>(null, headers, HttpStatus.NOT_FOUND);
        }

        headers.setCacheControl(CacheControl.noCache().getHeaderValue());
        headers.setContentType(MediaType.IMAGE_JPEG);
        return thumbnailService.getThumbnailForFile(fileName.get())
                .map(thumbnailEntity -> new ResponseEntity<>(thumbnailEntity.getThumbnail(), headers, HttpStatus.OK))
                .orElse(new ResponseEntity<>(null, headers, HttpStatus.OK));
    }
}
