package com.chiknas.swancloudserver.controllers;

import com.chiknas.swancloudserver.security.CurrentUser;
import com.chiknas.swancloudserver.services.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

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
                .ifPresent(lastFileUploadedDate -> model.addAttribute("lastFileUploadedDate", lastFileUploadedDate));
        return "index"; //view
    }

    @GetMapping("/preview/{id}")
    public String preview(Model model, @PathVariable String id) {
        fileService.getImageById(Integer.valueOf(id))
                .ifPresent(image -> model.addAttribute("image", Base64Utils.encodeToString(image)));
        return "image_preview"; //view
    }

    @PostMapping("/upload")
    public String uploadFiles(@RequestParam("files") List<MultipartFile> files, RedirectAttributes attributes) {
        files.stream().map(fileService::storeFile)
                .filter(Optional::isPresent)
                .flatMap(fileMetadata -> Stream.of(fileMetadata.get().getCreatedDate()))
                .max(LocalDate::compareTo)
                .ifPresent(currentUser::setLastUploadedFileDate);
        return "redirect:/";
    }
}
