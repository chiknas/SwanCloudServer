package com.chiknas.swancloudserver.services;

import com.chiknas.swancloudserver.dto.FileMetadataDTO;
import com.chiknas.swancloudserver.entities.FileMetadataEntity;
import com.chiknas.swancloudserver.entities.ThumbnailEntity;
import com.chiknas.swancloudserver.repositories.FileMetadataRepository;
import com.chiknas.swancloudserver.repositories.specifications.FileMetadataSpecification;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.chiknas.swancloudserver.services.helpers.FilesHelper.readFileToImage;

/**
 * Service that holds operations related to thumbnails.
 *
 * @author nkukn
 * @since 2/21/2021
 */
@Slf4j
@Service
public class ThumbnailService {

    private final FileMetadataRepository fileMetadataRepository;

    @Autowired
    public ThumbnailService(FileMetadataRepository fileMetadataRepository) {
        this.fileMetadataRepository = fileMetadataRepository;
    }

    /**
     * Goes through the database and tries to generate thumbnails for all the files that do not already have one.
     */
    public void resetThumbnails() {
        log.info("Thumbnail updates started!");
        long startTime = System.currentTimeMillis();

        Set<FileMetadataEntity> updatedFiles = fileMetadataRepository.findAll(
                        Specification.not(FileMetadataSpecification.hasThumbnail()),
                        Sort.by(Sort.Direction.DESC, "createdDate")
                )
                .parallelStream()
                .peek(fileMetadataEntity -> {
                    File file = Path.of(fileMetadataEntity.getPath()).toFile();
                    getThumbnail(file).ifPresent(fileMetadataEntity::setThumbnail);
                }).collect(Collectors.toSet());

        fileMetadataRepository.saveAll(updatedFiles);

        log.info("Thumbnail update completed in: {}seconds.", (System.currentTimeMillis() - startTime) / 1000);
    }

    
    @Async
    public void setThumbnailAsync(FileMetadataDTO fileMetadata) {
        CompletableFuture.runAsync(() ->
                fileMetadataRepository
                        .findById(fileMetadata.getId())
                        .ifPresent(fileMetadataEntity ->
                                getThumbnail(fileMetadataEntity.getFile())
                                        .ifPresent(th -> {
                                            fileMetadataEntity.setThumbnail(th);
                                            fileMetadataRepository.save(fileMetadataEntity);
                                        })
                        )
        );
    }

    /**
     * Tries to generate a thumbnail for the given file. Only images and videos are supported.
     */
    private Optional<ThumbnailEntity> getThumbnail(File file) {
        final Optional<BufferedImage> fileThumbnail = generateFileThumbnail(file);
        return fileThumbnail.map(thumbnail -> {
            final ThumbnailEntity entity = new ThumbnailEntity();
            entity.setThumbnail(ImageHelper.toByteArray(thumbnail));
            return entity;
        });
    }


    /**
     * Uses the file type and generates a thumbnail for a video/image. Empty if the thumbnail failed or the file is
     * not media.
     */
    private Optional<BufferedImage> generateFileThumbnail(File file) {

        try {
            BufferedImage thumbnail = new BufferedImage(320, 240, BufferedImage.TYPE_INT_RGB);
            String mimeType = Files.probeContentType(Path.of(file.getAbsolutePath()));

            if (mimeType == null) {
                return Optional.of(thumbnail);
            }

            if (mimeType.contains("video")) {
                thumbnail = getVideoThumbnail(file);
            } else if (mimeType.contains("image")) {
                thumbnail = getImageThumbnail(file);
            }

            return Optional.ofNullable(thumbnail);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return Optional.empty();
    }

    /**
     * Tries to create a thumbnail for the given video file. Will fail if file is not a video.
     */
    private BufferedImage getVideoThumbnail(File file) {
        try {
            FFmpegFrameGrabber g = new FFmpegFrameGrabber(file.getAbsolutePath());
            g.setFormat("mp4");
            g.start();
            final BufferedImage image = new Java2DFrameConverter().convert(g.grabImage());
            g.close();
            return image;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Tries to create a thumbnail for the given file. If the file is not an image it will return an
     * empty BufferedImage.
     */
    private BufferedImage getImageThumbnail(File file) {
        BufferedImage thumbnail = new BufferedImage(320, 240, BufferedImage.TYPE_INT_RGB);
        Optional.ofNullable(readFileToImage(file)).ifPresent(image ->
                thumbnail.createGraphics()
                        .drawImage(image.getScaledInstance(320, 240, Image.SCALE_DEFAULT), 0, 0, null));
        return thumbnail;
    }
}
