package com.chiknas.swancloudserver.services;

import com.chiknas.swancloudserver.dto.FileMetadataDTO;
import com.chiknas.swancloudserver.entities.ThumbnailEntity;
import com.chiknas.swancloudserver.repositories.FileMetadataRepository;
import com.chiknas.swancloudserver.repositories.specifications.FileMetadataSpecification;
import com.chiknas.swancloudserver.services.helpers.ImageHelper;
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
import java.util.concurrent.CompletableFuture;

import static com.chiknas.swancloudserver.services.helpers.ImageHelper.readFileToImage;

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

        fileMetadataRepository.findAll(
                        Specification.not(FileMetadataSpecification.hasThumbnail()),
                        Sort.by(Sort.Direction.DESC, "createdDate")
                )
                .parallelStream()
                .forEach(fileMetadataEntity -> {
                    File file = Path.of(fileMetadataEntity.getPath()).toFile();
                    getThumbnail(file).ifPresent(fileMetadataEntity::setThumbnail);
                    fileMetadataRepository.saveAndFlush(fileMetadataEntity);
                });

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
        try (FFmpegFrameGrabber g = new FFmpegFrameGrabber(file.getAbsolutePath())) {
            g.setFormat("mp4");
            g.start();
            return getImageFromFrame(g.grabImage());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    private BufferedImage getImageFromFrame(org.bytedeco.javacv.Frame frame) {
        try (final Java2DFrameConverter frameConverter = new Java2DFrameConverter()) {
            return frameConverter.convert(frame);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Tries to create a thumbnail for the given file. If the file is not an image it will return an
     * empty BufferedImage.
     */
    private BufferedImage getImageThumbnail(File file) {
        int thumbnailWidth = 320;
        int thumbnailHeight = 240;
        return Optional.ofNullable(readFileToImage(file))
                .map(image -> resizeImage(image, thumbnailWidth, thumbnailHeight))
                .orElse(new BufferedImage(thumbnailWidth, thumbnailHeight, BufferedImage.TYPE_INT_RGB));
    }

    private BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = resizedImage.createGraphics();
        graphics2D.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        graphics2D.dispose();
        return resizedImage;
    }
}
