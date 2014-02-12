package com.sismics.util;

import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

import static java.nio.file.StandardWatchEventKinds.*;

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
        for (WatchEvent object : events) {
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
                WatchKey key = dir.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
