package com.chiknas.swancloudserver.controllers;

import com.chiknas.swancloudserver.dto.FileMetadataDTO;
import com.chiknas.swancloudserver.services.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author nkukn
 * @since 3/28/2021
 */
@RestController
@RequestMapping("api")
public class ThumbnailController {

    private final FileService fileService;

    @Autowired
    public ThumbnailController(FileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping("/files/thumbnail/{id}")
    @ResponseBody
    public byte[] getFileThumbnail(@PathVariable Integer id) {
        return fileService.getFileById(id)
                .flatMap(FileMetadataDTO::getThumbnail)
                .orElse(null);

    }

}
