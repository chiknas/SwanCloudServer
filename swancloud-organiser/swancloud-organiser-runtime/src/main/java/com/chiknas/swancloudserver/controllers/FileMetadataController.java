package com.chiknas.swancloudserver.controllers;

import com.chiknas.swancloudserver.controllers.dto.FileMetadataApiDTO;
import com.chiknas.swancloudserver.dto.SetFileDateDTO;
import com.chiknas.swancloudserver.security.CurrentUser;
import com.chiknas.swancloudserver.services.FileMetadataFilter;
import com.chiknas.swancloudserver.services.FileOrganiserService;
import com.chiknas.swancloudserver.services.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("api")
public class FileMetadataController {

    private final FileService fileService;
    private final FileOrganiserService fileOrganiserService;
    private final CurrentUser currentUser;
    private final ConversionService conversionService;

    @Autowired
    public FileMetadataController(FileService fileService, FileOrganiserService fileOrganiserService,
                                  CurrentUser currentUser, ConversionService conversionService) {
        this.fileService = fileService;
        this.fileOrganiserService = fileOrganiserService;
        this.currentUser = currentUser;
        this.conversionService = conversionService;
    }

    @PostMapping("/upload")
    public void handleFileUpload(@RequestPart("files") List<MultipartFile> files) {
        files.stream().map(fileService::storeFile)
                .filter(Optional::isPresent)
                .flatMap(fileMetadata -> Stream.of(fileMetadata.get().getCreatedDate()))
                .max(LocalDateTime::compareTo)
                .ifPresent(currentUser::setLastUploadedFileDate);
        currentUser.setLastUploadedDate(LocalDateTime.now());
    }

    @PostMapping("/file/set-date")
    public void setFileDate(@RequestBody SetFileDateDTO fileDTO) {
        fileOrganiserService.reCategorizeFile(fileDTO.getFileId(), fileDTO.getCreationDate());
    }

    @GetMapping("/files")
    public List<FileMetadataApiDTO> getFiles(
            @RequestParam int limit,
            @RequestParam int offset,
            @RequestParam(required = false) Boolean uncategorized,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate beforeDate
    ) {
        FileMetadataFilter fileMetadataFilter = new FileMetadataFilter();
        Optional.ofNullable(uncategorized).ifPresent(fileMetadataFilter::setUncategorized);
        // Set current day included: time to be midnight so we get all files in the current day
        Optional.ofNullable(beforeDate).map(date -> date.plusDays(1).atStartOfDay()).ifPresent(fileMetadataFilter::setBeforeDate);
        return fileService.findAllFilesMetadata(limit, offset, fileMetadataFilter)
                .stream()
                .map(fileMetadataDTO -> conversionService.convert(fileMetadataDTO, FileMetadataApiDTO.class))
                .collect(Collectors.toList());
    }

}
