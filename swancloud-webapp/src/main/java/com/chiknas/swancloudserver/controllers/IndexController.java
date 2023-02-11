package com.chiknas.swancloudserver.controllers;

import com.chiknas.swancloudserver.security.CurrentUser;
import com.chiknas.swancloudserver.services.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

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
        fileService.getFileById(Integer.valueOf(id))
                .ifPresent(image -> model.addAttribute("image", Base64Utils.encodeToString(image)));
        return "image_preview"; //view
    }
}
