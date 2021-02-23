package com.chiknas.swancloudserver.controllers;

import com.chiknas.swancloudserver.dto.FileMetadataDTO;
import com.chiknas.swancloudserver.dto.SetFileDateDTO;
import com.chiknas.swancloudserver.repositories.cursorpagination.CursorPage;
import com.chiknas.swancloudserver.repositories.cursorpagination.CursorUtils;
import com.chiknas.swancloudserver.repositories.cursorpagination.cursors.FileMetadataCursor;
import com.chiknas.swancloudserver.services.FileOrganiserService;
import com.chiknas.swancloudserver.services.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
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
        fileOrganiserService.reCategorizeFile(fileDTO.getFileId(), fileDTO.getCreationDate());
    }

    @GetMapping("/files")
    public CursorPage<FileMetadataDTO> getFiles(@RequestParam(required = false)
                                                        String cursor,
                                                @RequestParam int limit) {
        return fileService.findAllFilesMetadata((FileMetadataCursor) CursorUtils.base64ToCursor(cursor), limit, false);
    }

    @GetMapping("/files/uncategorized")
    public CursorPage<FileMetadataDTO> getUncategorizedFiles(@RequestParam(required = false)
                                                                     String cursor,
                                                             @RequestParam int limit) {
        return fileService.findAllFilesMetadata((FileMetadataCursor) CursorUtils.base64ToCursor(cursor), limit, true);
    }

    @GetMapping("/files/thumbnail/{id}")
    public ResponseEntity<byte[]> getFileThumbnail(@PathVariable Integer id) {
        HttpHeaders headers = new HttpHeaders();
        return fileService.findFileMetadataById(id).map(metadata -> {
            headers.setCacheControl(CacheControl.noCache().getHeaderValue());
            headers.setContentType(MediaType.IMAGE_JPEG);
            return new ResponseEntity<>(metadata.getThumbnail(), headers, HttpStatus.OK);
        }).orElse(new ResponseEntity<>(null, headers, HttpStatus.NOT_FOUND));
    }
}
