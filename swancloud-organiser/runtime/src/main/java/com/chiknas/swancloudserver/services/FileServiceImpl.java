package com.chiknas.swancloudserver.services;

import com.chiknas.swancloudserver.dto.FileMetadataDTO;
import com.chiknas.swancloudserver.entities.FileMetadataEntity;
import com.chiknas.swancloudserver.repositories.FileMetadataRepository;
import com.chiknas.swancloudserver.repositories.OffsetPagedRequest;
import com.chiknas.swancloudserver.repositories.specifications.FileMetadataSpecification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.chiknas.swancloudserver.services.helpers.FilesHelper.readFileToImage;

@Slf4j
@Service
public class FileServiceImpl implements FileService {

    @Value("${files.base-path}")
    private final String filesBasePath = System.getProperty("user.dir");

    private final FileMetadataRepository fileMetadataRepository;
    private final ConversionService conversionService;
    private final ThumbnailService thumbnailService;
    private final IndexingService indexingService;

    @Autowired
    public FileServiceImpl(FileMetadataRepository fileMetadataRepository, ConversionService conversionService,
                           ThumbnailService thumbnailService, IndexingService indexingService) {
        this.fileMetadataRepository = fileMetadataRepository;
        this.conversionService = conversionService;
        this.thumbnailService = thumbnailService;
        this.indexingService = indexingService;
    }

    /**
     * Saves a multipart file in the current system drive. The directory used is ${files.base-path}
     */
    @Override
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

            // Index the file in the database
            indexingService.index(targetLocation.toFile());

        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }

    /**
     * Returns the metadata for all the files that are currently in the system.
     */
    @Override
    public List<FileMetadataDTO> findAllFilesMetadata(int limit, int offset, FileMetadataFilter filter) {

        Specification<FileMetadataEntity> specification = Optional.ofNullable(filter)
                .map(fileMetadataFilter ->
                        FileMetadataSpecification.forUncategorized(filter.getUncategorized())
                                .and(FileMetadataSpecification.forBeforeDate(filter.getBeforeDate())))
                .orElse(Specification.where(null));

        return fileMetadataRepository.findAll(
                        specification,
                        new OffsetPagedRequest(limit, offset, Sort.by(Sort.Direction.DESC, "createdDate"))
                )
                .stream()
                .map(fileMetadata -> conversionService.convert(fileMetadata, FileMetadataDTO.class))
                .collect(Collectors.toList());
    }

    public Optional<FileMetadataEntity> findFileMetadataById(Integer id) {
        return fileMetadataRepository.findById(id);
    }

    /**
     * Returns the real image in byte form for the specified image id in the db.
     *
     * @param id - the {@link FileMetadataEntity} id
     * @return - byte array of the real image
     */
    public Optional<byte[]> getImageById(Integer id) {
        return findFileMetadataById(id).map(fileMetadata -> {
            final String path = fileMetadata.getPath();
            File imgPath = new File(path);
            BufferedImage bufferedImage = readFileToImage(imgPath);
            return ImageHelper.toByteArray(bufferedImage);
        });
    }


    public void moveFile(File file, Path path) {
        moveFile(file, path, LocalDate.EPOCH);
    }

    public void moveFile(Integer fileId, Path path, LocalDate createdDate) {
        fileMetadataRepository.findById(fileId)
                .ifPresent(fileMetadata -> moveFile(new File(fileMetadata.getPath()), path, createdDate));
    }

    /**
     * Moves the specified file to the new path. Make sure the path exists before calling this method.
     * The system will try to figure out the file creation date from the file metadata. if this is already known,
     * pass the createdDate. It will also try to update the thumbnail if possible.
     */
    public void moveFile(File file, Path path, LocalDate createdDate) {
        try {
            File moveLocation = new File(path + "/" + file.getName());
            Files.move(file.toPath(), moveLocation.toPath(), StandardCopyOption.REPLACE_EXISTING);

            // update metadata if they exist or create new entry if it doesn't
            fileMetadataRepository.findByFileName(moveLocation.getName()).ifPresentOrElse(
                    fileMetadata -> {
                        fileMetadata.setPath(moveLocation.getAbsolutePath());
                        if (createdDate != null) {
                            fileMetadata.setCreatedDate(createdDate);
                        }

                        thumbnailService.addThumbnail(fileMetadata);

                        fileMetadataRepository.save(fileMetadata);
                    },
                    () -> {
                        FileMetadataEntity fileMetadata =
                                Objects.requireNonNull(conversionService.convert(moveLocation,
                                        FileMetadataEntity.class));
                        if (createdDate != null) {
                            fileMetadata.setCreatedDate(createdDate);
                        }

                        thumbnailService.addThumbnail(fileMetadata);

                        fileMetadataRepository.save(fileMetadata);
                    }
            );
        } catch (Exception e) {
            log.error(String.format("Failed to move file: %s to path: %s", file.getName(), path), e);
        }
    }

}
