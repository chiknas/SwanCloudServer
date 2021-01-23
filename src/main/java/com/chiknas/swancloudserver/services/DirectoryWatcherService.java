package com.chiknas.swancloudserver.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

/**
 * Class that keeps an a eye on a folder specified by the directory.source to allow the program to pick up new files
 * for process.
 * @author nkukn
 * @since 1/23/2021
 */
@Slf4j
@Service
public class DirectoryWatcherService implements Runnable {

    @Value("${files.base-path}")
    private final String filesBasePath = System.getProperty("user.dir");

    private final FileOrganiserService fileOrganiserService;

    @Autowired
    public DirectoryWatcherService(FileOrganiserService fileOrganiserService) {
        this.fileOrganiserService = fileOrganiserService;
    }

    @Override
    public void run() {
        try {
            WatchService watchService = FileSystems.getDefault().newWatchService();
            Path srcDirectory = Path.of(filesBasePath);
            srcDirectory.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);

            WatchKey key;
            while ((key = watchService.take()) != null) {
                for (WatchEvent<?> event : key.pollEvents()) {
                    File file = new File(filesBasePath + "/" + event.context());
                    if (!file.isDirectory()) {
                        fileOrganiserService.addFileToOrganiser(new File(filesBasePath + "/" + event.context()));
                    }
                }
                key.reset();
            }
        } catch (IOException | InterruptedException e) {
            log.error(e.getMessage(), e);
        }
    }
}
