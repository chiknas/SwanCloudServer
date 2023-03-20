package com.chiknas.swancloudserver.controllers;

import com.chiknas.swancloudserver.dto.FileMetadataDTO;
import com.chiknas.swancloudserver.security.CurrentUser;
import com.chiknas.swancloudserver.services.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.nio.file.Files;
import java.nio.file.Path;

@Controller
public class IndexController {

    private final FileService fileService;
    private final CurrentUser currentUser;

    @Autowired
    public IndexController(FileService fileService, CurrentUser currentUser) {
        this.fileService = fileService;
        this.currentUser = currentUser;
    }

    @GetMapping("/")
    public String index(Model model) {
        currentUser.getLastUploadedFileDate()
                .ifPresent(lastFileUploadedDate -> model.addAttribute("lastFileUploadedDate", lastFileUploadedDate.toLocalDate()));
        currentUser.getLastUploadedDate()
                .ifPresent(lastUploadedDate -> model.addAttribute("lastUploadedDate", lastUploadedDate.toLocalDate()));
        return "index"; //view
    }

    @GetMapping("/preview/{id}")
    public String preview(Model model, @PathVariable String id) {
        return fileService.getFileById(Integer.valueOf(id))
                .map(x -> getView(model, x))
                .orElseThrow(() -> new RuntimeException("Failed to find file with id: " + id));
    }

    private String getView(Model model, FileMetadataDTO fileMetadataDTO) {
        if (fileMetadataDTO.getFileMimeType().contains("image")) {
            return getImageView(model, fileMetadataDTO);
        }

        if (fileMetadataDTO.getFileMimeType().contains("video")) {
            return getVideoView(model, fileMetadataDTO);
        }

        throw new RuntimeException("Failed to generate view for file with id: " + fileMetadataDTO.getFile().getName());
    }

    private String getImageView(Model model, FileMetadataDTO fileMetadataDTO) {
        Path filePath = fileMetadataDTO.getFile().toPath();

        String base64Bytes = getBase64Bytes(filePath);
        model.addAttribute("image", base64Bytes);

        return "image_preview"; //view
    }

    private String getBase64Bytes(Path path) {
        try {
            return Base64Utils.encodeToString(Files.readAllBytes(path));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getVideoView(Model model, FileMetadataDTO fileMetadataDTO) {
        model.addAttribute("id", fileMetadataDTO.getId());

        return "video_preview";
    }


}
