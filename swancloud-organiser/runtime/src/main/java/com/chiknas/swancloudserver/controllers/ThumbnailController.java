package com.chiknas.swancloudserver.controllers;

import com.chiknas.swancloudserver.entities.FileMetadataEntity;
import com.chiknas.swancloudserver.entities.ThumbnailEntity;
import com.chiknas.swancloudserver.services.FileServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author nkukn
 * @since 3/28/2021
 */
@RestController
@RequestMapping("api")
public class ThumbnailController {

    private final FileServiceImpl fileService;

    @Autowired
    public ThumbnailController(FileServiceImpl fileService) {
        this.fileService = fileService;
    }

    @GetMapping("/files/thumbnail/{id}")
    @ResponseBody
    public byte[] getFileThumbnail(@PathVariable Integer id) {
        return fileService.findFileMetadataById(id)
                .map(FileMetadataEntity::getThumbnail)
                .map(ThumbnailEntity::getThumbnail)
                .orElse(null);

    }

    @GetMapping("/files/preview/{id}")
    @ResponseBody
    public byte[] getFilePreview(@PathVariable Integer id) {
        return fileService.getFileById(id).orElse(null);
    }
}
