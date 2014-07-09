package com.sismics.music.core.service.collection;

import com.google.common.collect.ImmutableSet;
import com.sismics.music.core.model.context.AppContext;
import com.sismics.music.core.model.dbi.Directory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
import java.util.Set;

/**
 * Collection visitor.
 *
 * @author jtremeaux
 */
public class CollectionVisitor extends SimpleFileVisitor<Path> {
    private static final Set<String> supportedExtSet = ImmutableSet.of(
            "mp3", "ogg", "oga", "aac", "m4a", "flac", "wav", "wma", "aif", "aiff", "ape", "mpc", "shn");

    /**
     * Root directory to visit.
     */
    private Directory rootDirectory;
    
    /**
     * Root path.
     */
    private Path rootPath;

    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(CollectionVisitor.class);

    public CollectionVisitor(Directory rootDirectory) {
        this.rootDirectory = rootDirectory;
        this.rootPath = Paths.get(rootDirectory.getLocation());
    }

    @Override
    public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
        String ext = com.google.common.io.Files.getFileExtension(path.toString()).toLowerCase();
        // Check that the extension is supported, and the file is not directly in the directory
        if (supportedExtSet.contains(ext) && !rootPath.equals(path.getParent())) {
            final CollectionService collectionService = AppContext.getInstance().getCollectionService();
            collectionService.indexFile(rootDirectory, path);
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    /**
     * Index subfolders in the root directory.
     * Subsubfolders are not indexed.
     */
    public void index() {
        try {
            Files.walkFileTree(rootPath, EnumSet.noneOf(FileVisitOption.class), 2, this);
        } catch (IOException e) {
            log.error("Cannot read from directory: " + rootDirectory.getLocation());
            return;
        }
    }
}
