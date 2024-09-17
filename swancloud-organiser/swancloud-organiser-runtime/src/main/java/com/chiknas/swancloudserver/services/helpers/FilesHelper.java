package com.chiknas.swancloudserver.services.helpers;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.lang.GeoLocation;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import com.drew.metadata.mp4.Mp4Directory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
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

    public static Optional<LocalDateTime> getCreationDate(MultipartFile file) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(file.getInputStream());
            return extractCreationDate(metadata);
        } catch (ImageProcessingException | IOException e) {
            log.error(e.getMessage().concat(file.getName()), e);
        }

        return Optional.empty();
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
            return extractCreationDate(metadata);
        } catch (ImageProcessingException | IOException e) {
            log.error(e.getMessage().concat(file.getName()), e);
        }

        return Optional.empty();
    }

    /**
     * Searches the metadata of the file to find the location a media file was taken.
     * Uses {@link GpsDirectory}
     */
    public static Optional<GeoLocation> getGeolocation(File file) {
        return extractMetadata(file)
                .flatMap(metadata -> {
                    GpsDirectory exifSubIFDDirectory = metadata.getFirstDirectoryOfType(GpsDirectory.class);
                    return Optional.ofNullable(exifSubIFDDirectory).map(GpsDirectory::getGeoLocation);
                });
    }


    private static Optional<Metadata> extractMetadata(File file) {
        try {
            return Optional.of(ImageMetadataReader.readMetadata(file));
        } catch (Exception e) {
            log.error("Failed reading metadata for file: " + file, e);
            return Optional.empty();
        }
    }

    private static Optional<LocalDateTime> extractCreationDate(Metadata metadata) {
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
            return Optional.of(Instant.ofEpochMilli(result.getTime()).atZone(ZoneId.of(ZoneOffset.UTC.getId())).toLocalDateTime());
        }

        return Optional.empty();
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
