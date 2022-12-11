package com.chiknas.swancloudserver;

import com.chiknas.swancloudserver.security.CurrentUser;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Mock current user to be used by integration test fixtures
 */
public class CurrentUserIntegrationTestHelper implements CurrentUser {
    @Override
    public Optional<LocalDateTime> getLastUploadedFileDate() {
        return Optional.empty();
    }

    @Override
    public void setLastUploadedFileDate(LocalDateTime localDate) {

    }

    @Override
    public Optional<String> getSyncUserQR() {
        return Optional.empty();
    }
}
