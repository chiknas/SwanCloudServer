package integration;

import com.chiknas.swancloudserver.integration.AbstractEmptyFileSystemIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.FileInputStream;

import static com.chiknas.swancloudserver.integration.IntegrationTestHelper.getTestResource;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests the main happy paths of the server related to files uploaded.
 * Ensures we can upload images and videos.
 * Ensures we can view these files listed on the api.
 */
@TestPropertySource(properties = {"files.base-path=src/test/resources/emptymockfilesystem/data"})
public class FileUploadEmptyFileSystemIntegrationTest extends AbstractEmptyFileSystemIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void uploadAndViewFile() throws Exception {

        // Setup a test image as multipart file to upload
        MockMultipartFile firstFile = new MockMultipartFile(
                "files", "test_image.jpg",
                MediaType.MULTIPART_FORM_DATA_VALUE,
                new FileInputStream(getTestResource("test_image.jpg").toFile()));

        // Hit the endpoint to upload the image
        mockMvc.perform(
                MockMvcRequestBuilders
                        .multipart("/api/upload")
                        .file(firstFile)
        ).andExpect(status().isOk()).andReturn();

        // Hit endpoint to verify the image is uploaded and visible in the api
        mockMvc
                .perform(
                        get("/api/files?limit=1&offset=0")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].fileMimeType", is("image/jpeg")))
                .andExpect(jsonPath("$[0].fileName", is("test_image.jpg")))
                .andExpect(jsonPath("$[0].createdDate", is("2016-09-05T12:31:58")))
                .andReturn();
    }


    /**
     * Tests that an upload of a file that already exists will return the existing file in the system.
     * This should be determined by the file name.
     */
    @Test
    void uploadAndViewDuplicatedFile() throws Exception {

        // Setup a test image as multipart file to upload
        MockMultipartFile firstFile = new MockMultipartFile(
                "files", "test_image.jpg",
                MediaType.MULTIPART_FORM_DATA_VALUE,
                new FileInputStream(getTestResource("test_image.jpg").toFile()));

        // Hit the endpoint to upload the image
        mockMvc.perform(
                MockMvcRequestBuilders
                        .multipart("/api/upload")
                        .file(firstFile)
        ).andExpect(status().isOk()).andReturn();

        // Hit the endpoint again with the same image
        mockMvc.perform(
                MockMvcRequestBuilders
                        .multipart("/api/upload")
                        .file(firstFile)
        ).andExpect(status().isOk()).andReturn();

        // Hit endpoint to get a list of files. set limit to 2 and make sure
        // only a single file is returned.
        mockMvc
                .perform(
                        get("/api/files?limit=2&offset=0")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].fileMimeType", is("image/jpeg")))
                .andExpect(jsonPath("$[0].fileName", is("test_image.jpg")))
                .andExpect(jsonPath("$[0].createdDate", is("2016-09-05T12:31:58")))
                .andReturn();
    }
}
