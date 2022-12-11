package com.chiknas.swancloudserver.controllers;

import com.chiknas.swancloudserver.dto.response.CurrentUserResponse;
import com.chiknas.swancloudserver.security.CurrentUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Base64;

@RestController
@RequestMapping("api")
public class CurrentUserController {

    private final CurrentUser currentUser;

    @Autowired
    public CurrentUserController(CurrentUser currentUser) {
        this.currentUser = currentUser;
    }


    @GetMapping("/userdetails")
    public CurrentUserResponse getCurrentUserDetails() {
        CurrentUserResponse currentUserResponse = new CurrentUserResponse();
        currentUserResponse.setLastUploadedFileDate(
                currentUser.getLastUploadedFileDate().orElse(LocalDate.EPOCH.atStartOfDay())
        );
        return currentUserResponse;
    }


    @GetMapping("/syncqr")
    public byte[] syncQrCode() {
        return currentUser.getSyncUserQR()
                .map(qr -> Base64.getDecoder().decode(qr.getBytes(StandardCharsets.UTF_8)))
                .orElseThrow(() -> new RuntimeException("Unable to generate Sync QR code."));
    }
}
