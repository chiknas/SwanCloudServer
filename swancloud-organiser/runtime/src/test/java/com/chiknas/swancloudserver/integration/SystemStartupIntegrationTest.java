package com.chiknas.swancloudserver.integration;

import com.chiknas.swancloudserver.ApplicationStartUpSequence;
import org.json.JSONArray;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * Test class responsible to test that the system will perform all the start up operation {@link ApplicationStartUpSequence} correctly.
 * 1. the system should index existing files
 * 2. the system should create thumbnails for existing files
 */
public class SystemStartupIntegrationTest extends AbstractFileSystemIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testExistingImageInTheAPI() throws Exception {
        // Hit endpoint to verify the image is visible in the api
        MvcResult mvcResult = mockMvc
                .perform(
                        get("/api/files?limit=1&offset=0")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].fileName", is("test_image1.jpg")))
                .andExpect(jsonPath("$[0].createdDate", is("2016-09-05")))
                .andReturn();

        String contentAsString = mvcResult.getResponse().getContentAsString();
        JSONArray json = new JSONArray(contentAsString);
        String imageId = json.getJSONObject(0).getString("id");

        // Hit endpoint to verify the image has a thumbnail
        MvcResult imageThumbnailResult = mockMvc
                .perform(
                        get("/api/files/thumbnail/" + imageId)
                )
                .andReturn();

        assertFalse(imageThumbnailResult.getResponse().getContentAsString().isEmpty());

    }
}
