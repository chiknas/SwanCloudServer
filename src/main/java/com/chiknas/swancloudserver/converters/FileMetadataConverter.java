package com.chiknas.swancloudserver.converters;

import com.chiknas.swancloudserver.entities.FileMetadataEntity;
import com.chiknas.swancloudserver.services.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDate;

@Slf4j
@Service
public class FileMetadataConverter implements Converter<File, FileMetadataEntity> {

    @Override
    public FileMetadataEntity convert(File file) {

        FileMetadataEntity fileMetadataEntity = new FileMetadataEntity();
        fileMetadataEntity.setFileName(file.getName());
        fileMetadataEntity.setPath(file.getAbsolutePath());
        FileService.getCreationDate(file).ifPresentOrElse(fileMetadataEntity::setCreatedDate,
                () -> fileMetadataEntity.setCreatedDate(LocalDate.EPOCH));

        // creating the thumbnail is an expensive operation to do here.
        // this should be picked up after this is saved to the db.
        fileMetadataEntity.setThumbnail(null);
        return fileMetadataEntity;
    }

    //TODO: https://stackoverflow.com/questions/59280534/image-is-90-degree-left-rotated-after-encoding-to-base64
    //TODO: use image metadata to rotate to the correct orientation for portraits
    public static BufferedImage rotateClockwise90(BufferedImage src) {
        int width = src.getWidth();
        int height = src.getHeight();

        BufferedImage dest = new BufferedImage(height, width, src.getType());

        Graphics2D graphics2D = dest.createGraphics();
        graphics2D.translate((height - width) / 2, (height - width) / 2);
        graphics2D.rotate(Math.PI / 2, height / 2, width / 2);
        graphics2D.drawRenderedImage(src, null);

        return dest;
    }
}
