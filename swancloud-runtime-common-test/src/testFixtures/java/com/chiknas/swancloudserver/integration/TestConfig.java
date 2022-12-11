package com.chiknas.swancloudserver.integration;

import com.chiknas.swancloudserver.CurrentUserIntegrationTestHelper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan({"com.chiknas.swancloudserver"})
class TestConfig {

    @Bean
    public CurrentUserIntegrationTestHelper mockCurrentUser() {
        return new CurrentUserIntegrationTestHelper();
    }
}
