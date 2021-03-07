package com.chiknas.swancloudserver.services;

import com.chiknas.swancloudserver.dto.FileMetadataDTO;
import com.chiknas.swancloudserver.entities.FileMetadataEntity;
import com.chiknas.swancloudserver.repositories.FileMetadataRepository;
import com.chiknas.swancloudserver.repositories.cursorpagination.CursorPage;
import com.chiknas.swancloudserver.repositories.cursorpagination.CursorUtils;
import com.chiknas.swancloudserver.repositories.cursorpagination.cursors.FileMetadataCursor;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.mp4.Mp4Directory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
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
import java.util.stream.Collectors;

import static com.chiknas.swancloudserver.services.ThumbnailService.toByteArray;

@Slf4j
@Service
public class FileService {

    @Value("${files.base-path}")
    private final String filesBasePath = System.getProperty("user.dir");

    private final FileMetadataRepository fileMetadataRepository;
    private final ConversionService conversionService;
    private final ThumbnailService thumbnailService;

    @Autowired
    public FileService(FileMetadataRepository fileMetadataRepository, ConversionService conversionService, ThumbnailService thumbnailService) {
        this.fileMetadataRepository = fileMetadataRepository;
        this.conversionService = conversionService;
        this.thumbnailService = thumbnailService;
    }

    /**
     * Saves a multipart file in the current system drive. The directory used is ${files.base-path}
     */
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

    /**
     * Returns the metadata for all the files that are currently in the system.
     */
    public CursorPage<FileMetadataDTO> findAllFilesMetadata(FileMetadataCursor fileMetadataCursor, int limit, boolean uncategorized) {
        List<FileMetadataEntity> allAfterCursor;

        if (fileMetadataCursor != null) {
            allAfterCursor = uncategorized || fileMetadataCursor.getCreatedDate().equals(LocalDate.EPOCH)
                    ? fileMetadataRepository
                    .findAllUncategorizedAfterCursor(PageRequest.of(0, limit + 1), fileMetadataCursor.getId())
                    : fileMetadataRepository
                    .findAllAfterCursor(PageRequest.of(0, limit + 1), fileMetadataCursor.getId(), fileMetadataCursor.getCreatedDate());
        } else {
            FileMetadataEntity fileMetadataEntity = new FileMetadataEntity();
            fileMetadataEntity.setCreatedDate(LocalDate.EPOCH);

            allAfterCursor = uncategorized
                    ? fileMetadataRepository
                    .findAll(Example.of(fileMetadataEntity), PageRequest.of(0, limit + 1, Sort.by(Sort.Direction.DESC, "id"))).getContent()
                    : fileMetadataRepository
                    .findAll(PageRequest.of(0, limit + 1, Sort.by(Sort.Direction.DESC, "createdDate"))).getContent();
        }

        return new CursorPage<>() {

            @Override
            public String getNextCursor() {
                return allAfterCursor.size() > limit
                        ? CursorUtils.toBase64(FileMetadataCursor.builder()
                        .id(allAfterCursor.get(allAfterCursor.size() - 1).getId())
                        .createdDate(allAfterCursor.get(allAfterCursor.size() - 1).getCreatedDate())
                        .build())
                        : null;
            }

            @Override
            public List<FileMetadataDTO> getNodes() {
                final List<FileMetadataEntity> fileMetadataEntities = allAfterCursor.size() > limit ? allAfterCursor.subList(0, limit) : allAfterCursor;
                return fileMetadataEntities.stream().map(metadata -> conversionService.convert(metadata, FileMetadataDTO.class)).collect(Collectors.toList());
            }
        };
    }

    public Optional<FileMetadataEntity> findFileMetadataById(Integer id) {
        return fileMetadataRepository.findById(id);
    }

    /**
     * Tries to read media file metadata (exif) to return the date the media was created.
     *
     * @param file - the file in the system you are interested in.
     * @return optional date because the metadata might not be present in the file.
     */
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
            log.error(e.getMessage().concat(file.getName()), e);
        }

        return Optional.empty();
    }

    /**
     * Tries to see if the file in question in still writing/reading by another source. Use this method with a grain of salt since we are not
     * always 100% sure if it will be correct.
     */
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
                        if (fileMetadata.getThumbnail() == null) {
                            final Optional<BufferedImage> fileThumbnail = thumbnailService.getFileThumbnail(Path.of(fileMetadata.getPath()).toFile());
                            fileThumbnail.ifPresent(thumbnail -> fileMetadata.setThumbnail(toByteArray(thumbnail)));
                        }
                        fileMetadataRepository.save(fileMetadata);
                    },
                    () -> {
                        FileMetadataEntity fileMetadata = Objects.requireNonNull(conversionService.convert(moveLocation, FileMetadataEntity.class));
                        if (createdDate != null) {
                            fileMetadata.setCreatedDate(createdDate);
                        }
                        if (fileMetadata.getThumbnail() == null) {
                            final Optional<BufferedImage> fileThumbnail = thumbnailService.getFileThumbnail(Path.of(fileMetadata.getPath()).toFile());
                            fileThumbnail.ifPresent(thumbnail -> fileMetadata.setThumbnail(toByteArray(thumbnail)));
                        }
                        fileMetadataRepository.save(fileMetadata);
                    }
            );
        } catch (Exception e) {
            log.error(String.format("Failed to move file: %s to path: %s", file.getName(), path), e);
        }
    }

}
