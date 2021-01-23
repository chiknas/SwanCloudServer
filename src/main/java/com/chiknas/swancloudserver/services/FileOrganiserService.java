package com.chiknas.swancloudserver.services;

import com.chiknas.swancloudserver.converters.FileMetadataConverter;
import com.chiknas.swancloudserver.repositories.FileMetadataRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;

/**
 * Organises the file passed in by year and month of the file's creation date.
 *
 * @author nkukn
 * @since 1/23/2021
 */
@Slf4j
@Service
public class FileOrganiserService {
    private final FileMetadataRepository fileMetadataRepository;
    private final FileMetadataConverter fileMetadataConverter;

    @Value("${files.base-path}")
    private final String filesBasePath = System.getProperty("user.dir");
    private final ArrayList<File> files = new ArrayList<>();

    @Autowired
    public FileOrganiserService(FileMetadataRepository fileMetadataRepository, FileMetadataConverter fileMetadataConverter) {
        this.fileMetadataRepository = fileMetadataRepository;
        this.fileMetadataConverter = fileMetadataConverter;
    }

    /**
     * Method to add files to the list to be organised by the system.
     */
    public void addFileToOrganiser(File file) {
        files.add(file);
        processList();
    }

    private void processList() {
        Iterator<File> iterator = files.iterator();
        while (iterator.hasNext()) {
            File file = iterator.next();
            if (!FileService.isFileInUse(file)) {
                processFile(file);
                iterator.remove();
            }
        }
    }

    /**
     * Organises the file to the correct folder by date. If date is not available nothing should happen.
     */
    private void processFile(File file) {
        Optional<LocalDate> optionalDate = FileService.getCreationDate(file);
        // TODO: write an orElse method that will move uncategorized images to an uncategorized folder for manual sorting
        optionalDate.ifPresent(date -> {
            File yearDir = new File(filesBasePath + "/" + date.getYear() + "/" + date.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH));
            if (!yearDir.exists()) {
                yearDir.mkdirs();
            }

            try {
                File fileOrganisedLocation = new File(yearDir.toPath() + "/" + file.getName());
                Files.move(file.toPath(), fileOrganisedLocation.toPath(), StandardCopyOption.ATOMIC_MOVE);

                // keep track of the file metadata if it doesn't already exists
                if (!fileMetadataRepository.existsByFileName(file.getName())) {
                    fileMetadataRepository.save(Objects.requireNonNull(fileMetadataConverter.convert(fileOrganisedLocation)));
                }
            } catch (IOException e) {
                log.error("Failed to move file: " + file.getName(), e);
            }
        });
    }
}
