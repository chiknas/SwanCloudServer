package com.chiknas.swancloudserver.services.helpers;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Directory;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

/**
 * @author nkukn
 * @since 5/2/2021
 */
@Slf4j
public class ImageHelper {

    private ImageHelper() {
        // static methods to help process images. no need to init.
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

    /**
     * Tries to read the file to a buffered image. Applies any format needed to create the correct BufferedImage.
     * If it fails to create the buffered image returns null instead
     */
    public static BufferedImage readFileToImage(File file) {
        try {
            BufferedImage bufferedImage = ImageIO.read(file);
            // ImageIO can't read the image's orientation, so it turns portraits into landscapes. Read the image
            // metadata and correct for that here.
            return getImageRotation(file).map(orientation -> {
                BufferedImage bufferedImage1 = ImageHelper.rotate90(bufferedImage);
                if (orientation == 1) {
                    return bufferedImage1;
                }

                BufferedImage bufferedImage2 = ImageHelper.rotate90(bufferedImage1);
                return ImageHelper.rotate90(bufferedImage2);
            }).orElse(bufferedImage);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }


    /**
     * Returns an integer that specifies if an image should be rotated or not to be at the state it was taken.
     * For example, if a portrait is saved as a landscape this method will return the rotation that needs to be made
     * to account for that.
     *
     * @param file - the image file to check
     * @return Empty optional = no rotation needed image is fine (or file is not an image or image doesn't have the
     * proper metadata to find its orientation)
     * 1 = rotate image 90 degrees clockwise
     * -1 = rotate image 90 degrees anti=clockwise
     */
    private static Optional<Integer> getImageRotation(File file) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(file);

            ExifIFD0Directory exifIFD0 = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
            if (exifIFD0 != null) {
                int orientation = exifIFD0.getInt(ExifIFD0Directory.TAG_ORIENTATION);
                switch (orientation) {
                    case 1: // [Exif IFD0] Orientation - Top, left side (Horizontal / normal)
                    case 3: // [Exif IFD0] Orientation - Bottom, right side (Rotate 180)
                        return Optional.empty();
                    case 6: // [Exif IFD0] Orientation - Right side, top (Rotate 90 CW)
                        return Optional.of(1);
                    case 8: // [Exif IFD0] Orientation - Left side, bottom (Rotate 270 CW)
                        return Optional.of(-1);
                }
            }

        } catch (ImageProcessingException | IOException | MetadataException e) {
            log.error(e.getMessage().concat(file.getName()), e);
        }

        return Optional.empty();
    }


    private static BufferedImage rotate90(BufferedImage image) {
        int width = image.getWidth(null);
        int height = image.getHeight(null);
        BufferedImage temp = new BufferedImage(height, width, image.getType());
        Graphics2D g2 = temp.createGraphics();
        g2.rotate(Math.toRadians(90), height / 2, height / 2);
        g2.drawImage(image, 0, 0, Color.WHITE, null);
        g2.dispose();
        return temp;
    }
}
