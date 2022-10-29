package com.chiknas.swancloudserver.controllers;

import com.chiknas.swancloudserver.dto.FileMetadataDTO;
import com.chiknas.swancloudserver.dto.SetFileDateDTO;
import com.chiknas.swancloudserver.services.FileMetadataFilter;
import com.chiknas.swancloudserver.services.FileOrganiserService;
import com.chiknas.swancloudserver.services.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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
        fileOrganiserService.reCategorizeFile(fileDTO.getFileId(), fileDTO.getCreationDate());
    }

    @GetMapping("/files")
    public List<FileMetadataDTO> getFiles(
            @RequestParam int limit,
            @RequestParam int offset,
            @RequestParam(required = false) Boolean uncategorized,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate beforeDate
    ) {
        FileMetadataFilter fileMetadataFilter = new FileMetadataFilter();
        Optional.ofNullable(uncategorized).ifPresent(fileMetadataFilter::setUncategorized);
        Optional.ofNullable(beforeDate).ifPresent(fileMetadataFilter::setBeforeDate);
        return fileService.findAllFilesMetadata(limit, offset, fileMetadataFilter);
    }

}
