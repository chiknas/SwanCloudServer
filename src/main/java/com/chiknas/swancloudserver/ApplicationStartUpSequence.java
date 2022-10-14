package com.chiknas.swancloudserver;

import com.chiknas.swancloudserver.services.IndexingService;
import com.chiknas.swancloudserver.services.ThumbnailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.NoSuchElementException;

@Slf4j
@Component
public class ApplicationStartUpSequence implements ApplicationListener<ApplicationReadyEvent> {

    private final IndexingService indexingService;
    private final ThumbnailService thumbnailService;
    private final DirectoryWatcherService directoryWatcherService;

    @Autowired
    public ApplicationStartUpSequence(IndexingService indexingService, ThumbnailService thumbnailService, DirectoryWatcherService directoryWatcherService) {
        this.indexingService = indexingService;
        this.thumbnailService = thumbnailService;
        this.directoryWatcherService = directoryWatcherService;
    }

    // Run operations that are required for the proper flow of the application here
    @PostConstruct
    public void startUp() {
        indexingService.resetIndexes();

        new Thread(directoryWatcherService).start();
    }

    // Only run non crucial/expensive operations here
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        thumbnailService.resetThumbnails();
    }
}
