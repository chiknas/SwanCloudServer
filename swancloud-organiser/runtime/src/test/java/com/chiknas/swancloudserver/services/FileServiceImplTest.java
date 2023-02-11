package com.chiknas.swancloudserver.services;

import com.chiknas.swancloudserver.repositories.FileMetadataRepository;
import com.chiknas.swancloudserver.repositories.OffsetPagedRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileServiceImplTest {

    @Mock
    private FileMetadataRepository fileMetadataRepository;
    @Mock
    private ConversionService conversionService;
    @Mock
    private FileOrganiserService fileOrganiserService;

    private FileService fileService;

    @BeforeEach
    void setUp() {
        fileService = new FileServiceImpl(fileMetadataRepository, conversionService, fileOrganiserService);
    }

    // Tests we can retrieve ALL metadata for files with a null filter
    @Test
    void findAllFilesMetadataUnfiltered() {
        // When
        // Calling the metadata repo through the service
        when(fileMetadataRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());

        fileService.findAllFilesMetadata(10, 0, null);

        // Then
        ArgumentCaptor<OffsetPagedRequest> offsetPagedRequestArgumentCaptor = ArgumentCaptor.forClass(OffsetPagedRequest.class);
        ArgumentCaptor<Specification> specificationArgumentCaptor = ArgumentCaptor.forClass(Specification.class);
        verify(fileMetadataRepository, times(1)).findAll(specificationArgumentCaptor.capture(), offsetPagedRequestArgumentCaptor.capture());
        // Ensure the page request has the specified values
        assertEquals(10, offsetPagedRequestArgumentCaptor.getValue().getPageSize());
        assertEquals(0, offsetPagedRequestArgumentCaptor.getValue().getOffset());
        // And the filter/specification is empty
        assertEquals(Specification.where(null), specificationArgumentCaptor.getValue());
    }

    // Tests ONLY uncategorized metadata is returned when the filter is set and the uncategorized flag is on
    @Test
    void findAllFilesMetadataUncategorizedFilterOn() {
        //Given
        // A filter with uncategorized set to true
        FileMetadataFilter fileMetadataFilter = new FileMetadataFilter();
        fileMetadataFilter.setUncategorized(true);


        // When
        // Calling the metadata repo through the service
        when(fileMetadataRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());

        fileService.findAllFilesMetadata(10, 0, fileMetadataFilter);

        // Then
        ArgumentCaptor<OffsetPagedRequest> offsetPagedRequestArgumentCaptor = ArgumentCaptor.forClass(OffsetPagedRequest.class);
        ArgumentCaptor<Specification> specificationArgumentCaptor = ArgumentCaptor.forClass(Specification.class);
        verify(fileMetadataRepository, times(1)).findAll(specificationArgumentCaptor.capture(), offsetPagedRequestArgumentCaptor.capture());
        // Ensure the page request has the specified values
        assertEquals(10, offsetPagedRequestArgumentCaptor.getValue().getPageSize());
        assertEquals(0, offsetPagedRequestArgumentCaptor.getValue().getOffset());
    }

    // Tests we can retrieve ALL data when the filter is set and uncategorized flag is off
    @Test
    void findAllFilesMetadataUncategorizedFilterOff() {
        //Given
        // A filter with uncategorized set to true
        FileMetadataFilter fileMetadataFilter = new FileMetadataFilter();
        fileMetadataFilter.setUncategorized(false);


        // When
        // Calling the metadata repo through the service
        when(fileMetadataRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());

        fileService.findAllFilesMetadata(10, 0, fileMetadataFilter);

        // Then
        ArgumentCaptor<OffsetPagedRequest> offsetPagedRequestArgumentCaptor = ArgumentCaptor.forClass(OffsetPagedRequest.class);
        ArgumentCaptor<Specification> specificationArgumentCaptor = ArgumentCaptor.forClass(Specification.class);
        verify(fileMetadataRepository, times(1)).findAll(specificationArgumentCaptor.capture(), offsetPagedRequestArgumentCaptor.capture());
        // Ensure the page request has the specified values
        assertEquals(10, offsetPagedRequestArgumentCaptor.getValue().getPageSize());
        assertEquals(0, offsetPagedRequestArgumentCaptor.getValue().getOffset());
    }
}