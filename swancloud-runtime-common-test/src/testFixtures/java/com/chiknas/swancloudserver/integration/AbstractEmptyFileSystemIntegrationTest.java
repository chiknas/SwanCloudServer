package com.chiknas.swancloudserver.integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;

/**
 * Abstract class to be extended but all integration tests. Responsible to setup the system correctly for testing.
 * Responsible to:
 * <ul>
 *     <li>Setup a new folder to store images/files</li>
 *     <li>Point the system the generated image folder</li>
 *     <li>Clean up after ourselves. Destroy the folder and all items inside it</li>
 * </ul>
 */
@SpringBootTest(properties = {"files.base-path=src/test/resources/emptymockfilesystem/data"}, classes = TestConfig.class)
@AutoConfigureMockMvc
public class AbstractEmptyFileSystemIntegrationTest {

    @Value("${files.base-path}")
    String basePath;

    @BeforeEach
    void setUp() {
        File theDir = new File(basePath);
        if (!theDir.exists()) {
            theDir.mkdirs();
        }
    }

    @AfterEach
    void tearDown() throws IOException {
        // Clean up the files that might have been uploaded during testing
        FileSystemUtils.deleteRecursively(new File(basePath));
    }

}
