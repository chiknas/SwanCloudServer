package com.chiknas.swancloudserver.controllers;

import com.chiknas.swancloudserver.services.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;
import java.util.List;

@Controller
public class WelcomeController {

    private final FileService fileService;

    private List<String> tasks = Arrays.asList("a", "b", "c", "d", "e", "f", "g");

    @Autowired
    public WelcomeController(FileService fileService) {
        this.fileService = fileService;
    }

    // /hello?name=kotlin
    @GetMapping("/")
    public String mainWithParam(
            @RequestParam(name = "name", required = false, defaultValue = "")
            String name, Model model) {

        model.addAttribute("message", name);

        return "welcome"; //view
    }

}
