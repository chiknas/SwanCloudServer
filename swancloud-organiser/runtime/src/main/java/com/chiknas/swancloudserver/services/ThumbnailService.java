package com.chiknas.swancloudserver.services;

import com.chiknas.swancloudserver.entities.FileMetadataEntity;
import com.chiknas.swancloudserver.entities.ThumbnailEntity;
import com.chiknas.swancloudserver.repositories.FileMetadataRepository;
import com.chiknas.swancloudserver.repositories.ThumbnailRepository;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.chiknas.swancloudserver.services.helpers.FilesHelper.readFileToImage;

/**
 * Service that holds operations related to thumbnails. We can also run this service in a thread to create all the
 * thumbnails on
 * files that do not have a thumbnail in the background. Only one can run at the background, if you try to run it
 * more than once it
 * will not do anything.
 *
 * @author nkukn
 * @since 2/21/2021
 */
@Slf4j
@Service
public class ThumbnailService {

    private final FileMetadataRepository fileMetadataRepository;
    private final ThumbnailRepository thumbnailRepository;

    @Autowired
    public ThumbnailService(FileMetadataRepository fileMetadataRepository, ThumbnailRepository thumbnailRepository) {
        this.fileMetadataRepository = fileMetadataRepository;
        this.thumbnailRepository = thumbnailRepository;
    }

    /**
     * Goes through the database and tries to generate thumbnails for all the files that do not already have one.
     */
    public void resetThumbnails() {
        log.info("Thumbnail updates started!");
        long startTime = System.currentTimeMillis();

        List<ThumbnailEntity> newThumbnails = new ArrayList<>();

        fileMetadataRepository.findAll(Sort.by(Sort.Direction.DESC, "createdDate"))
                .parallelStream()
                .parallel()
                .forEach(fileMetadataEntity -> addThumbnail(fileMetadataEntity)
                        .ifPresent(newThumbnails::add));

        log.info("Thumbnail update completed in: {}sec for {} new thumbnails.",
                (System.currentTimeMillis() - startTime) / 1000, newThumbnails.size());
    }

    /**
     * Returns a thumbnail entity for the given file name if found. empty optional otherwise.
     */
    public Optional<ThumbnailEntity> getThumbnailForFile(String fileName) {
        return thumbnailRepository.findByFileName(fileName);
    }

    /**
     * Will generate and persist a new {@link ThumbnailEntity} for the given file metadata. If a thumbnail
     * with the given name already exists it doesn't do anything.
     */
    public Optional<ThumbnailEntity> addThumbnail(FileMetadataEntity fileMetadataEntity) {
        if (!thumbnailRepository.existsByFileName(fileMetadataEntity.getFileName())) {
            final File file = Path.of(fileMetadataEntity.getPath()).toFile();
            final Optional<BufferedImage> fileThumbnail = generateFileThumbnail(file);
            final Optional<ThumbnailEntity> thumbnailEntity = fileThumbnail.map(thumbnail -> {
                final ThumbnailEntity entity = new ThumbnailEntity();
                entity.setThumbnail(ImageHelper.toByteArray(thumbnail));
                entity.setFileName(fileMetadataEntity.getFileName());
                return entity;
            });
            return thumbnailEntity.map(thumbnailRepository::save);
        }
        return Optional.empty();
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