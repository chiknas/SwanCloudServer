package com.chiknas.swancloudserver.controllers;

import com.chiknas.swancloudserver.entities.FileMetadataEntity;
import com.chiknas.swancloudserver.entities.ThumbnailEntity;
import com.chiknas.swancloudserver.services.FileService;
import com.chiknas.swancloudserver.services.ThumbnailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    @ResponseBody
    public byte[] getFileThumbnail(@PathVariable Integer id) {
        final Optional<String> fileName = fileService.findFileMetadataById(id).map(FileMetadataEntity::getFileName);

        if (fileName.isEmpty()) {
            return null;
        }

        return thumbnailService.getThumbnailForFile(fileName.get())
                .map(ThumbnailEntity::getThumbnail)
                .orElse(null);
    }
}
