package com.sismics.music.core.service.collection;

import com.google.common.collect.ImmutableSet;
import com.sismics.music.core.model.context.AppContext;
import com.sismics.music.core.model.dbi.Directory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Set;

/**
 * Collection visitor.
 *
 * @author jtremeaux
 */
public class CollectionVisitor extends SimpleFileVisitor<Path> {
    private static final Set<String> supportedExtSet = ImmutableSet.of(
            "ogg", "oga", "aac", "m4a", "flac", "wav", "wma", "aif", "aiff", "ape", "mpc", "shn", "mp3");

    /**
     * Root directory to visit.
     */
    private Directory rootDirectory;

    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(CollectionVisitor.class);

    public CollectionVisitor(Directory rootDirectory) {
        this.rootDirectory = rootDirectory;
    }

    @Override
    public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
        String ext = com.google.common.io.Files.getFileExtension(path.toString()).toLowerCase();
        if (supportedExtSet.contains(ext)) {
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
     * Index recursively the root directory.
     */
    public void index() {
        try {
            Files.walkFileTree(Paths.get(rootDirectory.getLocation()), this);
        } catch (IOException e) {
            log.error("Cannot read from directory: " + rootDirectory.getLocation());
            return;
        }
    }
}
