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

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Service that holds operations related to thumbnails. We can also run this service in a thread to create all the thumbnails on
 * files that do not have a thumbnail in the background. Only one can run at the background, if you try to run it more than once it
 * will not do anything.
 *
 * @author nkukn
 * @since 2/21/2021
 */
@Slf4j
@Service
public class ThumbnailService implements Runnable {

    // if this service is running in a separate thread?
    private static final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final FileMetadataRepository fileMetadataRepository;
    private final ThumbnailRepository thumbnailRepository;

    @Autowired
    public ThumbnailService(FileMetadataRepository fileMetadataRepository, ThumbnailRepository thumbnailRepository) {
        this.fileMetadataRepository = fileMetadataRepository;
        this.thumbnailRepository = thumbnailRepository;
    }

    @Override
    public void run() {

        // can not run more than once at the same time.
        if (isRunning.getAcquire()) return;

        isRunning.setRelease(true);

        try {
            log.info("Starting updating of files thumbnails!");

            long startTime = System.currentTimeMillis();

            List<ThumbnailEntity> newThumbnails = new ArrayList<>();

            fileMetadataRepository.findAll(Sort.by(Sort.Direction.DESC, "createdDate"))
                    .forEach(fileMetadataEntity -> addThumbnail(fileMetadataEntity).ifPresent(newThumbnails::add));

            log.info("Thumbnail update completed in: {}sec for {} new thumbnails.", (System.currentTimeMillis() - startTime) / 1000, newThumbnails.size());

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            isRunning.setRelease(false);
        }
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
                entity.setThumbnail(toByteArray(thumbnail));
                entity.setFileName(fileMetadataEntity.getFileName());
                return entity;
            });
            thumbnailEntity.ifPresent(thumbnailRepository::save);
            return thumbnailEntity;
        }
        return Optional.empty();
    }

    /**
     * Uses the file type and generates a thumbnail for a video/image. Empty if the thumbnail failed or the file is not media.
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
     * Tries to create a thumbnail for the given image file. Will fail if file is not a image.
     */
    private BufferedImage getImageThumbnail(File file) {
        try {
            BufferedImage thumbnail = new BufferedImage(320, 240, BufferedImage.TYPE_INT_RGB);
            thumbnail.createGraphics().drawImage(ImageIO.read(file).getScaledInstance(320, 240, Image.SCALE_DEFAULT), 0,
                    0, null);
            return thumbnail;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public static byte[] toByteArray(BufferedImage bi) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bi, "jpg", baos);
            return baos.toByteArray();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public static boolean isRunning() {
        return isRunning.getAcquire();
    }
}
