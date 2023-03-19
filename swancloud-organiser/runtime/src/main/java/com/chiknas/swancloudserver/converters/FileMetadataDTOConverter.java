package com.chiknas.swancloudserver.converters;

import com.chiknas.swancloudserver.dto.FileMetadataDTO;
import com.chiknas.swancloudserver.entities.FileMetadataEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.boot.web.server.MimeMappings;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;

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
            public LocalDateTime getCreatedDate() {
                return source.getCreatedDate();
            }

            @Override
            public File getFile() {
                return source.getFile();
            }

            @Override
            public String getFileMimeType() {
                String extension = FilenameUtils.getExtension(getFile().getName());
                return MimeMappings.DEFAULT.get(extension);
            }

            @Override
            public byte[] getThumbnail() {
                return source.getThumbnail().getThumbnail();
            }
        };
    }
}
