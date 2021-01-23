package com.chiknas.swancloudserver.services;

import com.chiknas.swancloudserver.entities.FileMetadataEntity;
import com.chiknas.swancloudserver.repositories.FileMetadataRepository;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.mp4.Mp4Directory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
public class FileService {

    @Value("${files.base-path}")
    private final String filesBasePath = System.getProperty("user.dir");

    private final FileMetadataRepository fileMetadataRepository;

    @Autowired
    public FileService(FileMetadataRepository fileMetadataRepository) {
        this.fileMetadataRepository = fileMetadataRepository;
    }

    public void storeFile(MultipartFile file) {
        // Normalize file name
        String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

        try {
            // Check if the file's name contains invalid characters
            if (fileName.contains("..")) {
                throw new RuntimeException("Sorry! Filename contains invalid path sequence " + fileName);
            }

            // Copy file to the target location (Replacing existing file with the same name)
            Path targetLocation = Path.of(filesBasePath).resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }

    public List<FileMetadataEntity> findAllFilesMetadata() {
        return fileMetadataRepository.findAll(Sort.by(Sort.Direction.DESC, "createdDate"));
    }

    public static Optional<LocalDate> getCreationDate(File file) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(file);

            Date result = null;

            ExifSubIFDDirectory exifSubIFDDirectory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
            if (exifSubIFDDirectory != null) {
                result = exifSubIFDDirectory.getDateOriginal();
            }

            Mp4Directory mp4Directory = metadata.getFirstDirectoryOfType(Mp4Directory.class);
            if (mp4Directory != null) {
                result = mp4Directory.getDate(Mp4Directory.TAG_CREATION_TIME);
            }

            if (result != null) {
                return Optional.of(new java.sql.Date(result.getTime()).toLocalDate());
            }
        } catch (ImageProcessingException | IOException e) {
            log.error(e.getMessage(), e);
        }

        return Optional.empty();
    }

    public static boolean isFileInUse(File file) {
        try {
            //give some space in case the file is still copying
            Thread.sleep(100L);
            FileInputStream fileInputStream = new FileInputStream(file);
            //noinspection ResultOfMethodCallIgnored this is to check if the file is open by another process
            fileInputStream.available();
            fileInputStream.close();
            return false;
        } catch (IOException | InterruptedException e) {
            log.error(e.getMessage(), e);
        }
        return true;
    }

}
