package com.sismics.util;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

/**
 * Test the WatchService from Java 7.
 *
 * @author jtremeaux
 */
public class TestWatchService {

    @Test
    @Ignore
    public void watchServiceTest() throws Exception {
        WatchService watchService = FileSystems.getDefault().newWatchService();
        Path path = FileSystems.getDefault().getPath("/tmp/watchdir");
        registerAll(watchService, path);

        WatchKey key = watchService.take();
        List<WatchEvent<?>> events = key.pollEvents();
        for (WatchEvent<?> object : events) {
            if (object.kind() == ENTRY_MODIFY) {
                System.out.println("Modify: " + path.toRealPath() +"/"+ object.context().toString());

            }
            if (object.kind() == ENTRY_DELETE) {
                System.out.println("Delete: " +  path.toRealPath() +"/"+ object.context().toString());
            }
            if (object.kind() == ENTRY_CREATE) {
                System.out.println("Created: " +  path.toRealPath() +"/"+ object.context().toString());
            }
        }
    }

    private void registerAll(final WatchService watchService, final Path start) throws IOException {
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
        @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                dir.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
