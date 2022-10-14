package com.chiknas.swancloudserver.converters;

import com.chiknas.swancloudserver.entities.FileMetadataEntity;
import com.chiknas.swancloudserver.services.helpers.FilesHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Service;

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
        FilesHelper.getCreationDate(file).ifPresentOrElse(fileMetadataEntity::setCreatedDate,
                () -> fileMetadataEntity.setCreatedDate(LocalDate.EPOCH));

        return fileMetadataEntity;
    }

}
