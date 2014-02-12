package com.sismics.music.core.service.collection;

import com.sismics.music.core.model.context.AppContext;
import com.sismics.music.core.model.dbi.Directory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Collection visitor.
 *
 * @author jtremeaux
 */
public class CollectionVisitor extends SimpleFileVisitor<Path> {
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
        if (path.toString().endsWith(".mp3")) {
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
