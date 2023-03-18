package com.chiknas.swancloudserver.services;

import com.chiknas.swancloudserver.dto.FileMetadataDTO;
import com.chiknas.swancloudserver.entities.FileMetadataEntity;
import com.chiknas.swancloudserver.repositories.FileMetadataRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
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
        LocalDateTime creationDate = getCreationDate(file).orElse(LocalDate.now().atStartOfDay());
        Path pathFromDate = createPathFromDate(creationDate);
        File fileDiscLocation = pathFromDate.resolve(fileName).toFile();

        File savedFile = saveFileToDisc(file, fileDiscLocation);

        Optional<FileMetadataDTO> fileMetadataDTO = indexingService.index(savedFile);

        fileMetadataDTO.ifPresent(thumbnailService::setThumbnailAsync);

        return fileMetadataDTO;
    }

    /**
     * Saves the specified file to the location on the disc.
     * Uses Input/Output stream to be able to handle large files
     * without loading them to memory.
     * Misuse of a file here can result in {@link OutOfMemoryError}
     */
    private File saveFileToDisc(MultipartFile fileToSave, File fileDiscLocation) {

        try (FileOutputStream outStream = new FileOutputStream(fileDiscLocation)) {

            int readByteCount = 0;
            byte[] bufferedBytes = new byte[1024];

            BufferedInputStream fileInputStream = new BufferedInputStream(fileToSave.getInputStream());

            while ((readByteCount = fileInputStream.read(bufferedBytes)) != -1) {
                outStream.write(bufferedBytes, 0, readByteCount);
            }

            return fileDiscLocation;

        } catch (Exception e) {
            throw new RuntimeException("Failed to save file to disc: " + fileDiscLocation, e);
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
