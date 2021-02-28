package com.chiknas.swancloudserver.services;

import com.chiknas.swancloudserver.converters.FileMetadataConverter;
import com.chiknas.swancloudserver.entities.FileMetadataEntity;
import com.chiknas.swancloudserver.repositories.FileMetadataRepository;
import com.chiknas.swancloudserver.utils.CustomFileVisitor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
    private final ThumbnailService thumbnailService;

    @Autowired
    public IndexingService(FileOrganiserService fileOrganiserService, FileMetadataConverter fileMetadataConverter, FileMetadataRepository fileMetadataRepository, ThumbnailService thumbnailService) {
        this.fileOrganiserService = fileOrganiserService;
        this.fileMetadataConverter = fileMetadataConverter;
        this.fileMetadataRepository = fileMetadataRepository;
        this.thumbnailService = thumbnailService;
    }

    @PostConstruct
    public void onStartUp() {
        run();
    }


    public void run() {
        fileMetadataRepository.deleteAll();

        List<Path> paths = new ArrayList<>();

        try {
            final CustomFileVisitor customFileVisitor = new CustomFileVisitor();
            Files.walkFileTree(Path.of(filesBasePath), customFileVisitor);
            paths = customFileVisitor.getPaths();
        } catch (Exception e) {
            log.error("Failed to read path files.", e);
        }


        log.info("Starting indexing of files.");
        long startTime = System.currentTimeMillis();

        final List<FileMetadataEntity> fileMetadataList = paths.stream().parallel().map(path -> {
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
                    return fileMetadata;
                }
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toUnmodifiableList());

        fileMetadataRepository.saveAll(fileMetadataList);

        log.info("Indexing completed in: {}sec", (System.currentTimeMillis() - startTime) / 1000);

        new Thread(thumbnailService).start();
    }
}
