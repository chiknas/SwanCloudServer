package com.chiknas.swancloudserver.utils;

import lombok.Getter;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom file visitor to add error handling for hidden files like the System volume information folder on the top directory of hard drives.
 *
 * @author nkukn
 * @since 2/28/2021
 */
@Getter
public class CustomFileVisitor extends SimpleFileVisitor<Path> {

    private final List<Path> paths = new ArrayList<>();

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
        paths.add(file);
        return FileVisitResult.CONTINUE;
    }

    // Add failure handling, skip folder is access is denied
    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        if (exc instanceof AccessDeniedException) {
            return FileVisitResult.SKIP_SUBTREE;
        }
        return super.visitFileFailed(file, exc);
    }
}
