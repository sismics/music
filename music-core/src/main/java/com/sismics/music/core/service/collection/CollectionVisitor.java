package com.sismics.music.core.service.collection;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sismics.music.core.constant.Constants;
import com.sismics.music.core.model.context.AppContext;
import com.sismics.music.core.model.dbi.Directory;

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
     * Root path.
     */
    private Path rootPath;
    
    /**
     * Files indexed during the lifetime of this visitor.
     */
    private Set<String> fileNameSet = new HashSet<>();

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
        // TODO Albumarts must be scanned too
        if (Constants.SUPPORTED_AUDIO_EXTENSIONS.contains(ext) && !rootPath.equals(path.getParent())) {
            final CollectionService collectionService = AppContext.getInstance().getCollectionService();
            collectionService.indexFile(rootDirectory, path);
            fileNameSet.add(path.toAbsolutePath().toString());
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        return FileVisitResult.CONTINUE;
    }
    
    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        log.error("Error visiting: " + file, exc);
        return FileVisitResult.SKIP_SUBTREE;
    }

    /**
     * Index subfolders in the root directory.
     * Subsubfolders are not indexed.
     */
    public void index() {
        try {
            Files.walkFileTree(rootPath, EnumSet.noneOf(FileVisitOption.class), 2, this);
        } catch (IOException e) {
            log.error("Cannot read from directory: " + rootDirectory.getLocation(), e);
            return;
        }
    }

    /**
     * Getter of fileNameSet.
     * 
     * @return fileNameSet
     */
    public Set<String> getFileNameSet() {
        return fileNameSet;
    }
}
