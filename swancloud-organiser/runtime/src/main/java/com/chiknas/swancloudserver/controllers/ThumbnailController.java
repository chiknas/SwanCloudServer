package com.chiknas.swancloudserver.controllers;

import com.chiknas.swancloudserver.entities.FileMetadataEntity;
import com.chiknas.swancloudserver.entities.ThumbnailEntity;
import com.chiknas.swancloudserver.services.FileServiceImpl;
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
    private final FileServiceImpl fileService;

    @Autowired
    public ThumbnailController(ThumbnailService thumbnailService, FileServiceImpl fileService) {
        this.thumbnailService = thumbnailService;
        this.fileService = fileService;
    }

    @GetMapping("/files/thumbnail/{id}")
    @ResponseBody
    public byte[] getFileThumbnail(@PathVariable Integer id) {
        final Optional<String> fileName = fileService.findFileMetadataById(id).map(FileMetadataEntity::getFileName);

        return fileName.flatMap(s -> thumbnailService.getThumbnailForFile(s)
                .map(ThumbnailEntity::getThumbnail)).orElse(null);

    }

    @GetMapping("/files/preview/{id}")
    @ResponseBody
    public byte[] getFilePreview(@PathVariable Integer id) {
        return fileService.getFileById(id).orElse(null);
    }
}
