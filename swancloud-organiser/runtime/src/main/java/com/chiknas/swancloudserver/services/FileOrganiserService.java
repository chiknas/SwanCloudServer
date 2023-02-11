package com.chiknas.swancloudserver.services;

import com.chiknas.swancloudserver.dto.FileMetadataDTO;
import com.chiknas.swancloudserver.entities.FileMetadataEntity;
import com.chiknas.swancloudserver.repositories.FileMetadataRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Optional;

import static com.chiknas.swancloudserver.services.helpers.FilesHelper.getCreationDate;

/**
 * Organises the file passed in by year and month of the file's creation date.
 *
 * @author nkukn
 * @since 1/23/2021
 */
@Slf4j
@Service
public class FileOrganiserService {

    private final ThumbnailService thumbnailService;
    private final IndexingService indexingService;
    private final FileMetadataRepository fileMetadataRepository;

    @Value("${files.base-path}")
    private final String filesBasePath = System.getProperty("user.dir");

    @Autowired
    public FileOrganiserService(ThumbnailService thumbnailService, IndexingService indexingService, FileMetadataRepository fileMetadataRepository) {
        this.thumbnailService = thumbnailService;
        this.indexingService = indexingService;
        this.fileMetadataRepository = fileMetadataRepository;
    }

    public Optional<FileMetadataDTO> categorizeFile(String fileName, MultipartFile file) {
        try {
            LocalDateTime creationDate = getCreationDate(file).orElse(LocalDate.now().atStartOfDay());
            Path pathFromDate = createPathFromDate(creationDate);

            File savedFile = Files.write(pathFromDate.resolve(fileName), file.getBytes()).toFile();

            Optional<FileMetadataDTO> fileMetadataDTO = indexingService.index(savedFile);

            fileMetadataDTO.ifPresent(thumbnailService::setThumbnailAsync);

            return fileMetadataDTO;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Re-categorizes a file based the passed in date.
     */
    public void reCategorizeFile(Integer fileId, LocalDateTime creationDate) {
        Optional<FileMetadataEntity> fileMetadataEntity = fileMetadataRepository
                .findById(fileId)
                .map(file -> {
                    try {
                        Path pathFromDate = createPathFromDate(creationDate);
                        Files.move(file.getFile().toPath(), pathFromDate.resolve(file.getFileName()));

                        file.setCreatedDate(creationDate);

                        return file;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
        fileMetadataEntity.ifPresent(fileMetadataRepository::save);
    }

    private Path createPathFromDate(LocalDateTime date) {
        File yearDir = LocalDate.EPOCH.atStartOfDay().equals(date)
                ? new File(filesBasePath + "/uncategorized")
                : new File(filesBasePath + "/" + date.getYear() + "/" + date.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH));
        yearDir.mkdirs();
        return yearDir.toPath();
    }
}
