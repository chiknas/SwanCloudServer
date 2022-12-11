package com.chiknas.swancloudserver.integration;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;


@AutoConfigureMockMvc
@SpringBootTest(properties = {"files.base-path=src/test/resources/mockfilesystem"}, classes = TestConfig.class)
public class AbstractFileSystemIntegrationTest {
}
