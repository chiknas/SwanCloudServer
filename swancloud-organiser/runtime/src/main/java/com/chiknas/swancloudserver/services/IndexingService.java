package com.chiknas.swancloudserver.services;

import com.chiknas.swancloudserver.dto.FileMetadataDTO;
import com.chiknas.swancloudserver.entities.FileMetadataEntity;
import com.chiknas.swancloudserver.repositories.FileMetadataRepository;
import com.chiknas.swancloudserver.services.helpers.FilesHelper;
import com.chiknas.swancloudserver.utils.CustomFileVisitor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Service responsible to re-index the base path the system is currently running on.
 *
 * @author nkukn
 * @since 1/24/2021
 */
@Slf4j
@Transactional
@Service
public class IndexingService {

    @Value("${files.base-path}")
    private final String filesBasePath = System.getProperty("user.dir");
    private final ConversionService conversionService;
    private final FileMetadataRepository fileMetadataRepository;

    @Autowired
    public IndexingService(ConversionService conversionService, FileMetadataRepository fileMetadataRepository) {
        this.conversionService = conversionService;
        this.fileMetadataRepository = fileMetadataRepository;
    }

    /**
     * Will index the file properties we need in the database. Location, filename, etc..
     * From now on the file is available in the system.
     */
    public Optional<FileMetadataDTO> index(File file) {
        if (file.isFile()) {
            // if the converter can not find the created date, figure out created date from the folder structure.
            FileMetadataEntity fileMetadata = Objects.requireNonNull(conversionService.convert(file, FileMetadataEntity.class));
            if (fileMetadata.getCreatedDate().equals(LocalDate.EPOCH)) {
                FilesHelper.getLocalDateFromPath(file)
                        .ifPresentOrElse(
                                fileMetadata::setCreatedDate,
                                () -> fileMetadata.setCreatedDate(LocalDate.EPOCH)
                        );
            }

            return Optional.ofNullable(conversionService.convert(fileMetadataRepository.save(fileMetadata), FileMetadataDTO.class));
        }
        return Optional.empty();
    }


    /**
     * Refreshes the indexes by dropping the metadata table and regenerating it by going through the whole filesystem
     * and finding every file.
     * Can be expensive use with care.
     */
    public void resetIndexes() {
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

        paths.stream().parallel().forEach(path -> {
            File file = new File(path.toString());
            // ignore directories
            if (!file.isFile()) return;

            index(file);
        });

        log.info("Indexing completed in: {}sec", (System.currentTimeMillis() - startTime) / 1000);
    }
}
