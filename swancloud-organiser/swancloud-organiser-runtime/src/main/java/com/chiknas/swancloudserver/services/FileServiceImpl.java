package com.chiknas.swancloudserver.services;

import com.chiknas.swancloudserver.dto.FileMetadataDTO;
import com.chiknas.swancloudserver.entities.FileMetadataEntity;
import com.chiknas.swancloudserver.repositories.FileMetadataRepository;
import com.chiknas.swancloudserver.repositories.OffsetPagedRequest;
import com.chiknas.swancloudserver.repositories.specifications.FileMetadataSpecification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FileServiceImpl implements FileService {

    private final FileMetadataRepository fileMetadataRepository;
    private final ConversionService conversionService;
    private final FileOrganiserService fileOrganiserService;

    @Autowired
    public FileServiceImpl(FileMetadataRepository fileMetadataRepository, ConversionService conversionService,
                           FileOrganiserService fileOrganiserService) {
        this.fileMetadataRepository = fileMetadataRepository;
        this.conversionService = conversionService;
        this.fileOrganiserService = fileOrganiserService;
    }

    /**
     * Saves a multipart file in the current system drive. The directory used is ${files.base-path}
     */
    @Override
    public Optional<FileMetadataDTO> storeFile(MultipartFile file) {
        // Normalize file name
        String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

        Optional<FileMetadataEntity> storedFile = fileMetadataRepository.findByFileName(fileName);
        if (storedFile.isPresent()) {
            return storedFile.map(f -> conversionService.convert(f, FileMetadataDTO.class));
        }

        // Check if the file's name contains invalid characters
        if (fileName.contains("..")) {
            throw new RuntimeException("Sorry! Filename contains invalid path sequence " + fileName);
        }

        return fileOrganiserService.categorizeFile(fileName, file);
    }

    /**
     * Returns the metadata for all the files that are currently in the system.
     */
    @Override
    public List<FileMetadataDTO> findAllFilesMetadata(int limit, int offset, FileMetadataFilter filter) {

        Specification<FileMetadataEntity> specification = Optional.ofNullable(filter)
                .map(fileMetadataFilter ->
                        FileMetadataSpecification.forUncategorized(filter.getUncategorized())
                                .and(FileMetadataSpecification.forBeforeDate(filter.getBeforeDate())))
                .orElse(Specification.where(null));

        return fileMetadataRepository.findAll(
                        specification,
                        new OffsetPagedRequest(limit, offset, Sort.by(Sort.Direction.DESC, "createdDate", "id"))
                )
                .stream()
                .map(fileMetadata -> conversionService.convert(fileMetadata, FileMetadataDTO.class))
                .collect(Collectors.toList());
    }

    public Optional<FileMetadataEntity> findFileMetadataById(Integer id) {
        return fileMetadataRepository.findById(id);
    }

    /**
     * Returns the file content in byte form for the specified file id in the db.
     *
     * @param id - the {@link FileMetadataEntity} id
     * @return - byte array of the real file
     */
    @Override
    public Optional<FileMetadataDTO> getFileById(Integer id) {
        return findFileMetadataById(id).map(x -> conversionService.convert(x, FileMetadataDTO.class));
    }

}
