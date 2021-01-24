package com.chiknas.swancloudserver.controllers;

import com.chiknas.swancloudserver.dto.SetFileDateDTO;
import com.chiknas.swancloudserver.entities.FileMetadataEntity;
import com.chiknas.swancloudserver.services.FileOrganiserService;
import com.chiknas.swancloudserver.services.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("api")
public class FileController {

    private final FileService fileService;
    private final FileOrganiserService fileOrganiserService;

    @Autowired
    public FileController(FileService fileService, FileOrganiserService fileOrganiserService) {
        this.fileService = fileService;
        this.fileOrganiserService = fileOrganiserService;
    }

    @PostMapping("/upload")
    public void handleFileUpload(@RequestPart("data") List<MultipartFile> files) {
        files.forEach(fileService::storeFile);
    }

    @PostMapping("/file/set-date")
    public void setFileDate(@RequestBody SetFileDateDTO fileDTO) {
        fileOrganiserService.categorizeFile(fileDTO.getFileId(), fileDTO.getCreationDate());
    }

    @GetMapping("/files")
    public List<FileMetadataEntity> getFiles() {
        return fileService.findAllFilesMetadata();
    }

    @GetMapping("/files/uncategorized")
    public List<FileMetadataEntity> getUncategorizedFiles() {
        return fileService.findAllUncategorizedFilesMetadata();
    }

}
