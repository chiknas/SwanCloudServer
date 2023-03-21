package com.chiknas.swancloudserver.converters;

import com.chiknas.swancloudserver.controllers.dto.FileMetadataApiDTO;
import com.chiknas.swancloudserver.dto.FileMetadataDTO;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class FileMetadataApiDTOConverter implements Converter<FileMetadataDTO, FileMetadataApiDTO> {
    @Override
    public FileMetadataApiDTO convert(FileMetadataDTO source) {
        return new FileMetadataApiDTO() {
            @Override
            public Integer getId() {
                return source.getId();
            }

            @Override
            public LocalDateTime getCreatedDate() {
                return source.getCreatedDate();
            }

            @Override
            public String getFileMimeType() {
                return source.getFileMimeType();
            }

            @Override
            public String getFileName() {
                return source.getFile().getName();
            }
        };
    }
}
