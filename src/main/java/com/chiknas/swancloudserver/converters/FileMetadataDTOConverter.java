package com.chiknas.swancloudserver.converters;

import com.chiknas.swancloudserver.dto.FileMetadataDTO;
import com.chiknas.swancloudserver.entities.FileMetadataEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * @author nkukn
 * @since 2/23/2021
 */
@Slf4j
@Service
public class FileMetadataDTOConverter implements Converter<FileMetadataEntity, FileMetadataDTO> {

    @Override
    public FileMetadataDTO convert(FileMetadataEntity source) {
        return new FileMetadataDTO() {
            @Override
            public Integer getId() {
                return source.getId();
            }

            @Override
            public String getFileName() {
                return source.getFileName();
            }

            @Override
            public LocalDate getCreatedDate() {
                return source.getCreatedDate();
            }
        };
    }
}
