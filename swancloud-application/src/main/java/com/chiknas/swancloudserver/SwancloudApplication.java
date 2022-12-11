package com.chiknas.swancloudserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * Entry point of the SwanCloudServer. This stitches all the necessary modules of the app together
 * through their configuration classes.
 * Use only runtime modules in here as anything else is only to be used at compile time.
 */
@Import({
        WebappConfiguration.class,
        OrganiserRuntimeConfiguration.class,
        SecurityConfiguration.class
})
@SpringBootApplication(scanBasePackages = "com.chiknas.swancloudserver",
        exclude = {SecurityAutoConfiguration.class})
public class SwancloudApplication {
    public static void main(String[] args) {
        SpringApplication.run(SwancloudApplication.class, args);
    }
}
