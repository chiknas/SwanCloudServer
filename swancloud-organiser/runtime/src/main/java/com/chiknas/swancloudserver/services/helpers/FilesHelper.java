package com.chiknas.swancloudserver.services.helpers;

import com.chiknas.swancloudserver.services.ImageHelper;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.mp4.Mp4Directory;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.*;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

/**
 * Helper function to perform on files in the file system. No external dependencies should be added in this class.
 */
@Slf4j
public class FilesHelper {

    private FilesHelper() {
        // No need to init. class to help work with files in the fielsystem.
    }

    /**
     * Tries to read media file metadata (exif) to return the date the media was created.
     *
     * @param file - the file in the system you are interested in.
     * @return optional date because the metadata might not be present in the file.
     */
    public static Optional<LocalDateTime> getCreationDate(File file) {
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
                return Optional.of(Instant.ofEpochMilli(result.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime());
            }
        } catch (ImageProcessingException | IOException e) {
            log.error(e.getMessage().concat(file.getName()), e);
        }

        return Optional.empty();
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
    public static Optional<Integer> getImageRotation(File file) {
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

    /**
     * Tries to see if the file in question in still writing/reading by another source. Use this method with a grain
     * of salt since we are not
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

    /**
     * Tries to read the file to a buffered image. Applies any format needed to create the correct BufferedImage.
     * If it fails to create the buffered image returns null instead
     */
    public static BufferedImage readFileToImage(File file) {
        try {
            BufferedImage bufferedImage = ImageIO.read(file);
            // ImageIO can't read the image's orientation, so it turns portraits into landscapes. Read the image
            // metadata and correct for that here.
            return getImageRotation(file).map(orientation -> ImageHelper.rotate90(bufferedImage,
                    orientation == 1)).orElse(bufferedImage);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    public static Optional<LocalDateTime> getLocalDateFromPath(File file) {
        String path = file.getAbsolutePath();
        Optional<Month> fileInMonth = Arrays.stream(Month.values())
                .filter(month -> path.toLowerCase().contains(month.toString().toLowerCase()))
                .findFirst();
        if (fileInMonth.isPresent()) {
            Month month = fileInMonth.get();
            int indexOfMonth = path.toLowerCase().indexOf(month.toString().toLowerCase());
            String yearString = path.substring(indexOfMonth - 5, indexOfMonth - 1);
            try {
                return Optional.of(LocalDate.of(Integer.parseInt(yearString), month, 1).atStartOfDay());
            } catch (NumberFormatException e) {
                log.error(String.format("Failed to get date from the current path: %s", path), e);
            }
        }
        return Optional.empty();
    }
}
