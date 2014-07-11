package com.sismics.music.core.service.collection;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.sismics.music.core.dao.dbi.DirectoryDao;
import com.sismics.music.core.model.dbi.Directory;
import com.sismics.music.core.util.TransactionUtil;

/**
 * Collection watch service.
 *
 * @author bgamard
 */
public class CollectionWatchService extends AbstractExecutionThreadService {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(CollectionWatchService.class);
    
    /**
     * NIO watch service.
     */
    private WatchService watchService;
    
    /**
     * Storage for watch keys.
     */
    Map<WatchKey, Path> watchKeyMap = Maps.newConcurrentMap();

    public CollectionWatchService() {
    }

    @Override
    protected void startUp() {
        TransactionUtil.handle(new Runnable() {
            @Override
            public void run() {
                // Create a NIO watch service
                try {
                    watchService = FileSystems.getDefault().newWatchService();
                } catch (IOException e) {
                    log.error("Cannot create a NIO watch service", e);
                    stopAsync();
                }
                
                // Start watching all directories
                DirectoryDao directoryDao = new DirectoryDao();
                List<Directory> directoryList = directoryDao.findAllEnabled();
                for (Directory directory : directoryList) {
                    watchDirectory(directory);
                }
            }
        });
    }
    
    /**
     * Watch a new directory.
     * TODO Call this from PUT /directory
     * 
     * @param directory Directory to watch
     */
    public void watchDirectory(final Directory directory) {
        try {
            Path path = Paths.get(directory.getLocation());
            Files.walkFileTree(path, EnumSet.noneOf(FileVisitOption.class), 2, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    WatchKey watchKey = dir.register(watchService, ENTRY_CREATE, ENTRY_DELETE);
                    watchKeyMap.put(watchKey, dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            log.error("Cannot watch directory: " + directory, e);
        }
    }
    
    /**
     * Unwatch a directory.
     * TODO Call this from DELETE /directory
     * 
     * @param directory Directory to unwatch
     */
    public void unwatchDirectory(final Directory directory) {
        Path path = Paths.get(directory.getLocation());
        
        for (Iterator<Entry<WatchKey, Path>> it = watchKeyMap.entrySet().iterator(); it.hasNext(); ) {
            Entry<WatchKey, Path> entry = it.next();
            if (entry.getValue().startsWith(path)) {
                entry.getKey().cancel();
                it.remove();
            }
        }
    }

    @Override
    protected void run() throws Exception {
        while (isRunning()) {
            WatchKey watchKey = watchService.take();
            
            Path dir = watchKeyMap.get(watchKey);
            if (dir == null) {
                continue;
            }
            
            for (WatchEvent<?> event : watchKey.pollEvents()) {
                @SuppressWarnings("unchecked")
                WatchEvent<Path> eventPath = (WatchEvent<Path>) event;
                WatchEvent.Kind<Path> kind = eventPath.kind();
                Path relativePath = eventPath.context();
                Path fullPath = dir.resolve(relativePath);
                
                if (kind == ENTRY_CREATE) {
                    if (Files.isDirectory(fullPath, LinkOption.NOFOLLOW_LINKS)) {
                        // TODO Watch the new subdirectory if it doesn't exceed the max depth watched (2)
                    } else {
                        // TODO New track added (refactor validation with CollectionVisitor, wait for filesize unchanged in 2sec)
                    }
                }
                
                if (kind == ENTRY_DELETE) {
                    if (!Files.isDirectory(fullPath, LinkOption.NOFOLLOW_LINKS)) {
                        // TODO Track removed
                    }
                }
            }
            
            if (!watchKey.reset()) {
                watchKeyMap.remove(watchKey);
            }
        }
    }
    
    @Override
    protected void shutDown() {
    }
}
