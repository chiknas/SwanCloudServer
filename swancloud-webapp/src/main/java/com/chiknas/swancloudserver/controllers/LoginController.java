package com.chiknas.swancloudserver.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String login(Model model) {
        return "login"; //view
    }

    @GetMapping("/access-denied")
    public String denied(Model model) {
        return "denied"; //view
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(Model model) {
        return "reset_password"; //view
    }

}
