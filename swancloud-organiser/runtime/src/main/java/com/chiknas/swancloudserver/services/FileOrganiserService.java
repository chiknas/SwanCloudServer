package com.chiknas.swancloudserver.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.Optional;

import static com.chiknas.swancloudserver.services.helpers.FilesHelper.getCreationDate;
import static com.chiknas.swancloudserver.services.helpers.FilesHelper.isFileInUse;

/**
 * Organises the file passed in by year and month of the file's creation date.
 *
 * @author nkukn
 * @since 1/23/2021
 */
@Slf4j
@Service
public class FileOrganiserService {

    private final FileServiceImpl fileService;

    @Value("${files.base-path}")
    private final String filesBasePath = System.getProperty("user.dir");
    private final ArrayList<File> files = new ArrayList<>();

    @Autowired
    public FileOrganiserService(FileServiceImpl fileService) {
        this.fileService = fileService;
    }

    /**
     * Method to add files to the list to be organised by the system.
     */
    public void addFileToOrganiser(File file) {
        files.add(file);
        processList();
    }

    /**
     * Re-categorizes a file based the passed in date.
     */
    public void reCategorizeFile(Integer fileId, LocalDate creationDate) {
        fileService.moveFile(fileId, createPathFromDate(creationDate), creationDate);
    }

    private void processList() {
        Iterator<File> iterator = files.iterator();
        while (iterator.hasNext()) {
            File file = iterator.next();
            if (!isFileInUse(file)) {
                processFile(file);
                iterator.remove();
            }
        }
    }

    /**
     * Organises the file to the correct folder by date. If date is not available it organises the file to generic uncategorized folder for manual categorization.
     */
    private void processFile(File file) {
        Optional<LocalDate> creationDate = getCreationDate(file);
        creationDate.ifPresentOrElse(date -> fileService.moveFile(file, createPathFromDate(date), date),
                () -> {
                    File uncategorizedDir = new File(filesBasePath + "/uncategorized");
                    uncategorizedDir.mkdirs();
                    fileService.moveFile(file, uncategorizedDir.toPath());
                });
    }

    private Path createPathFromDate(LocalDate date) {
        File yearDir = new File(filesBasePath + "/" + date.getYear() + "/" + date.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH));
        yearDir.mkdirs();
        return yearDir.toPath();
    }
}
