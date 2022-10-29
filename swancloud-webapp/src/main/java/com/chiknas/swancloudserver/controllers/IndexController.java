package com.chiknas.swancloudserver.controllers;

import com.chiknas.swancloudserver.services.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class IndexController {

    private final FileService fileService;

    @Autowired
    public IndexController(FileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping("/")
    public String index(Model model) {
        return "index"; //view
    }

    @PostMapping("/upload")
    public String uploadFiles(@RequestParam("files") List<MultipartFile> files, RedirectAttributes attributes) {
        files.forEach(fileService::storeFile);
        return "redirect:/";
    }
}
