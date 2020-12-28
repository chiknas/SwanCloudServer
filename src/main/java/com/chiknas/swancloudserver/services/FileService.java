package com.chiknas.swancloudserver.services;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

import com.chiknas.swancloudserver.converters.FileMetadataConverter;
import com.chiknas.swancloudserver.entities.FileMetadataEntity;
import com.chiknas.swancloudserver.repositories.FileMetadataRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileService {

    @Value("${files.base-path}")
    private String filesBasePath = System.getProperty("user.dir");

    private final FileMetadataConverter fileMetadataConverter;
    private final FileMetadataRepository fileMetadataRepository;

    @Autowired
    public FileService(FileMetadataRepository fileMetadataRepository, FileMetadataConverter fileMetadataConverter) {
        this.fileMetadataRepository = fileMetadataRepository;
        this.fileMetadataConverter = fileMetadataConverter;
    }

    public FileMetadataEntity storeFile(MultipartFile file) {
        // Normalize file name
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            // Check if the file's name contains invalid characters
            if (fileName.contains("..")) {
                throw new RuntimeException("Sorry! Filename contains invalid path sequence " + fileName);
            }

            // Copy file to the target location (Replacing existing file with the same name)
            Path targetLocation = Path.of(filesBasePath).resolve(fileName);

            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            File savedFile = targetLocation.toAbsolutePath().toFile();
            FileMetadataEntity existingMetadata = fileMetadataRepository.findByFileName(fileName);
            return existingMetadata != null ? existingMetadata
                    : fileMetadataRepository.save(fileMetadataConverter.convert(savedFile));

        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }

    public List<FileMetadataEntity> findAllFiles() {
        return fileMetadataRepository.findAll();
    }

}
