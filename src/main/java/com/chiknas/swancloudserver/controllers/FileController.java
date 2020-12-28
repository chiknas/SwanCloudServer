package com.chiknas.swancloudserver.controllers;

import java.io.IOException;
import java.util.List;

import com.chiknas.swancloudserver.entities.FileMetadataEntity;
import com.chiknas.swancloudserver.services.FileService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("api")
public class FileController {

	private final FileService fileService;

	@Autowired
	public FileController(FileService fileService) {
		this.fileService = fileService;
	}

	@PostMapping("/upload")
	public void handleFileUpload(@RequestParam("file") MultipartFile file) throws IOException {
		fileService.storeFile(file);
	}

	@GetMapping("/files")
	public List<FileMetadataEntity> getFiles() {
		return fileService.findAllFiles();
	}

}
