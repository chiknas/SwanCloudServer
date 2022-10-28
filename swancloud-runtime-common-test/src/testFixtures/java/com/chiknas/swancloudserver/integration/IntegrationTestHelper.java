package com.chiknas.swancloudserver.integration;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Helper functions to be used among integration tests.
 */
public class IntegrationTestHelper {

    private static final Path resourceDirectory = Paths.get("src", "test", "resources");

    public static Path getTestResource(String name) {
        return Paths.get(resourceDirectory.toString(), name);
    }
}
