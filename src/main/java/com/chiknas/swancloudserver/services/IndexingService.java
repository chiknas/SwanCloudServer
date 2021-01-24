package com.chiknas.swancloudserver.services;

import com.chiknas.swancloudserver.converters.FileMetadataConverter;
import com.chiknas.swancloudserver.entities.FileMetadataEntity;
import com.chiknas.swancloudserver.repositories.FileMetadataRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Service responsible to re-index the base path the system is currently running on.
 *
 * @author nkukn
 * @since 1/24/2021
 */
@Slf4j
@Service
public class IndexingService {

    @Value("${files.base-path}")
    private final String filesBasePath = System.getProperty("user.dir");

    private final FileOrganiserService fileOrganiserService;
    private final FileMetadataConverter fileMetadataConverter;
    private final FileMetadataRepository fileMetadataRepository;

    @Autowired
    public IndexingService(FileOrganiserService fileOrganiserService, FileMetadataConverter fileMetadataConverter, FileMetadataRepository fileMetadataRepository) {
        this.fileOrganiserService = fileOrganiserService;
        this.fileMetadataConverter = fileMetadataConverter;
        this.fileMetadataRepository = fileMetadataRepository;
    }

    @PostConstruct
    public void onStartUp() {
        run();
    }


    public void run() {
        fileMetadataRepository.deleteAll();

        try (Stream<Path> paths = Files.walk(Path.of(filesBasePath), Integer.MAX_VALUE)) {
            log.info("Starting indexing of files.");
            paths.forEach(path -> {
                File file = new File(path.toString());
                // ignore directories
                if (file.isFile()) {
                    // files in the base path have not been organized yet. the organiser will handle the indexing.
                    if (file.getAbsolutePath().equals(filesBasePath)) {
                        fileOrganiserService.addFileToOrganiser(file);
                    } else {
                        // if the converter can not find the created date, figure out created date from the folder structure.
                        FileMetadataEntity fileMetadata = Objects.requireNonNull(fileMetadataConverter.convert(file));
                        if (fileMetadata.getCreatedDate().equals(LocalDate.EPOCH)) {
                            FileOrganiserService.getLocalDateFromPath(file.getAbsolutePath())
                                    .ifPresent(fileMetadata::setCreatedDate);

                        }
                        fileMetadataRepository.save(fileMetadata);
                    }

                }
            });
            log.info("Indexing is complete!");
        } catch (IOException e) {
            log.error("Failed to index base path.", e);
        }
    }
}
