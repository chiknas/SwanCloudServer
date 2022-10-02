package com.chiknas.swancloudserver.services;

import com.chiknas.swancloudserver.entities.FileMetadataEntity;
import com.chiknas.swancloudserver.entities.ThumbnailEntity;
import com.chiknas.swancloudserver.repositories.FileMetadataRepository;
import com.chiknas.swancloudserver.repositories.ThumbnailRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

class ThumbnailServiceTest {

    @Mock
    private FileMetadataRepository fileMetadataRepository;
    @Mock
    private ThumbnailRepository thumbnailRepository;

    private ThumbnailService thumbnailService;

    @BeforeEach
    void setUp() {
        openMocks(this);
        thumbnailService = new ThumbnailService(fileMetadataRepository, thumbnailRepository);
    }

    @Test
    void getThumbnailForFile() {
        // Setup thumbnail entity for mock filename
        String fileName = "fileName";
        ThumbnailEntity thumbnail = new ThumbnailEntity();
        thumbnail.setFileName(fileName);

        // when searching thumbnail on filename
        when(thumbnailRepository.findByFileName(eq(fileName))).thenReturn(Optional.of(thumbnail));
        Optional<ThumbnailEntity> result = thumbnailService.getThumbnailForFile("fileName");

        // then the correct entity is returned
        assertTrue(result.isPresent());
        assertEquals(fileName, result.get().getFileName());
    }

    @Test
    void addThumbnail() {
        // Setup file metadata class to add a thumbnail for
        String fileName = "fileName";
        String pathToFile = "pathToFile";

        FileMetadataEntity fileMetadataEntity = new FileMetadataEntity();
        fileMetadataEntity.setFileName(fileName);
        fileMetadataEntity.setPath(pathToFile);

        // When creating a new thumbnail for a file return a mock entity
        ThumbnailEntity thumbnailEntityMock = mock(ThumbnailEntity.class);
        when(thumbnailRepository.existsByFileName(eq(fileName))).thenReturn(false);
        when(thumbnailRepository.save(any(ThumbnailEntity.class))).thenReturn(thumbnailEntityMock);

        // Then the mock entity is saved and returned
        Optional<ThumbnailEntity> result = thumbnailService.addThumbnail(fileMetadataEntity);
        assertTrue(result.isPresent());
        assertEquals(thumbnailEntityMock, result.get());

        verify(thumbnailRepository, times(1)).save(any(ThumbnailEntity.class));
    }

    @Test
    void addExistingThumbnail() {
        // Setup file metadata class to add a thumbnail for
        String fileName = "fileName";

        FileMetadataEntity fileMetadataEntity = new FileMetadataEntity();
        fileMetadataEntity.setFileName(fileName);

        // When creating a thumbnail but it already exists
        when(thumbnailRepository.existsByFileName(eq(fileName))).thenReturn(true);

        // Then it is not saved and the method returns empty
        Optional<ThumbnailEntity> result = thumbnailService.addThumbnail(fileMetadataEntity);
        assertTrue(result.isEmpty());

        verify(thumbnailRepository, times(0)).save(any(ThumbnailEntity.class));
    }
}