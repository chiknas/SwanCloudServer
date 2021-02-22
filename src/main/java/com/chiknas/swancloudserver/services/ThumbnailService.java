package com.chiknas.swancloudserver.services;

import com.chiknas.swancloudserver.repositories.FileMetadataRepository;
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
import java.util.Optional;

/**
 * @author nkukn
 * @since 2/21/2021
 */
@Slf4j
@Service
public class ThumbnailService implements Runnable {

    private final FileMetadataRepository fileMetadataRepository;

    @Autowired
    public ThumbnailService(FileMetadataRepository fileMetadataRepository) {
        this.fileMetadataRepository = fileMetadataRepository;
    }

    @Override
    public void run() {
        log.info("Starting updating of files thumbnails!");
        long startTime = System.currentTimeMillis();

        fileMetadataRepository.findAllByThumbnailNull(Sort.by(Sort.Direction.DESC, "createdDate")).forEach(fileMetadataEntity -> {
            final File file = Path.of(fileMetadataEntity.getPath()).toFile();
            final Optional<BufferedImage> fileThumbnail = getFileThumbnail(file);
            fileThumbnail.ifPresent(thumbnail -> {
                fileMetadataEntity.setThumbnail(toByteArray(thumbnail));
                fileMetadataRepository.save(fileMetadataEntity);
            });
        });

        log.info("Thumbnail update completed in: {}sec.", (System.currentTimeMillis() - startTime) / 1000);
    }

    /**
     * Uses the file type and generates a thumbnail for a video/image. Empty if the thumbnail failed or the file is not media.
     */
    public Optional<BufferedImage> getFileThumbnail(File file) {

        try {
            BufferedImage thumbnail = new BufferedImage(320, 240, BufferedImage.TYPE_INT_RGB);
            String mimeType = Files.probeContentType(Path.of(file.getAbsolutePath()));
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
}
