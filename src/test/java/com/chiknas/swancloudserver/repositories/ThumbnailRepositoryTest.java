package com.chiknas.swancloudserver.repositories;

import com.chiknas.swancloudserver.entities.ThumbnailEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ThumbnailRepositoryTest {

    private final String fileName = "fileName";

    @Autowired
    ThumbnailRepository thumbnailRepository;

    @BeforeEach
    void setUp() {
        // Create a generic thumbnail entity in the database
        ThumbnailEntity thumbnailEntity = new ThumbnailEntity();
        thumbnailEntity.setFileName(fileName);
        byte[] thumbnail = new byte[20];
        new Random().nextBytes(thumbnail);
        thumbnailEntity.setThumbnail(thumbnail);
        thumbnailRepository.saveAndFlush(thumbnailEntity);
    }

    @Test
    void existsByFileName() {
        assertTrue(thumbnailRepository.existsByFileName(fileName));
        assertFalse(thumbnailRepository.existsByFileName("randomFileName"));
    }

    @Test
    void findByFileName() {
        // The findByFileName method correctly returns the file with the exact filename
        Optional<ThumbnailEntity> byFileName = thumbnailRepository.findByFileName(fileName);

        assertTrue(byFileName.isPresent());
        assertEquals(fileName, byFileName.get().getFileName());
    }

    @Test
    void findByFileNameNull() {
        // We can search for null filename and the method will happily return an empty optional instead of failing
        Optional<ThumbnailEntity> byFileNameNull = thumbnailRepository.findByFileName(null);
        assertTrue(byFileNameNull.isEmpty());
    }
}