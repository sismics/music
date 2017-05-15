package com.sismics.music.core.service.collection;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.sismics.music.core.constant.Constants;
import com.sismics.music.core.dao.dbi.AlbumDao;
import com.sismics.music.core.dao.dbi.ArtistDao;
import com.sismics.music.core.dao.dbi.DirectoryDao;
import com.sismics.music.core.dao.dbi.TrackDao;
import com.sismics.music.core.model.context.AppContext;
import com.sismics.music.core.model.dbi.Directory;
import com.sismics.music.core.model.dbi.Track;
import com.sismics.music.core.util.TransactionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.Map.Entry;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;

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
    private Map<WatchKey, Path> watchKeyMap = Maps.newConcurrentMap();

    /**
     * Watched directories.
     */
    private List<Directory> watchedDirectoryList = Collections.synchronizedList(new ArrayList<Directory>());
    
    public CollectionWatchService() {
    }

    @Override
    protected void startUp() {
        TransactionUtil.handle(() -> {
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
        });
    }
    
    /**
     * Watch a new directory.
     * 
     * @param directory Directory to watch
     */
    public void watchDirectory(final Directory directory) {
        try {
            final Path path = Paths.get(directory.getLocation());
            Files.walkFileTree(path, EnumSet.noneOf(FileVisitOption.class), 2, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    watchPath(dir);
                    return FileVisitResult.CONTINUE;
                }
                
                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    log.error("Error visiting: " + file, exc);
                    return FileVisitResult.SKIP_SUBTREE;
                }
            });
            
            watchedDirectoryList.add(directory);
        } catch (IOException e) {
            log.error("Cannot watch directory: " + directory, e);
        }
    }
    
    /**
     * Unwatch a directory.
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
        
        watchedDirectoryList.remove(directory);
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
                Path path = dir.resolve(eventPath.context());
                final Directory directory = getParentDirectory(path);
                Path directoryPath = Paths.get(directory.getLocation());
                
                if (kind == ENTRY_CREATE) {
                    if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
                        // Watch the new directory if it's not too deep, and index it fully
                        if (path.getNameCount() - directoryPath.getNameCount() == 1) {
                            log.info("New directory created, watching and indexing it: " + path);
                            watchPath(path);
                            indexFolder(directory, path);
                        }
                    } else {
                        indexNewFile(directory, path);
                    }
                }
                
                if (kind == ENTRY_DELETE) {
                    pathRemoved(directory, path);
                }
            }
            
            cleanupOrphans();
            
            if (!watchKey.reset()) {
                watchKeyMap.remove(watchKey);
            }
        }
    }
    
    @Override
    protected void shutDown() {
        try {
            watchService.close();
        } catch (IOException e) {
            log.error("Error stopping watch service", e);
        }
    }
    
    /**
     * Watch a new path.
     * 
     * @param path Path
     */
    private void watchPath(Path path) throws IOException {
        watchKeyMap.put(path.register(watchService, ENTRY_CREATE, ENTRY_DELETE), path);
    }
    
    /**
     * Index a new file if it is an audio track.
     * 
     * @param directory Directory
     * @param file File
     */
    private void indexNewFile(final Directory directory, final Path file) {
        // Validate the file for indexing
        // TODO Albumarts must be watched too
        String ext = com.google.common.io.Files.getFileExtension(file.toString()).toLowerCase();
        Path directoryPath = Paths.get(directory.getLocation());
        if (!Constants.SUPPORTED_AUDIO_EXTENSIONS.contains(ext) || directoryPath.equals(file.getParent())) {
            return;
        }
        
        log.info("New audio file created, indexing it: " + file);
        
        // Wait until the file does not grow
        boolean isGrowing = true;
        do {
            long initialWeight = file.toFile().length();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.error("Interrupted while sleeping", e);
            }
            long finalWeight = file.toFile().length();
            isGrowing = initialWeight < finalWeight;
        } while(isGrowing);
        
        // Add the audio file to the index
        TransactionUtil.handle(() -> AppContext.getInstance().getCollectionService().indexFile(directory, file));
    }
    
    /**
     * Index the immediate children of a folder. 
     * 
     * @param directory Directory
     * @param path Path to folder
     */
    private void indexFolder(final Directory directory, final Path path) throws IOException {
        Files.walkFileTree(path, EnumSet.noneOf(FileVisitOption.class), 1, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                indexNewFile(directory, file);
                return FileVisitResult.CONTINUE;
            }
            
            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                log.error("Error visiting: " + file, exc);
                return FileVisitResult.SKIP_SUBTREE;
            }
        });
    }
    
    /**
     * A path has been removed from a watched directory.
     * It can be a single track or a whole directory.
     * 
     * @param directory Directory
     * @param path File
     */
    private void pathRemoved(final Directory directory, final Path path) {
        TransactionUtil.handle(() -> {
            // Search all tracks included in the deleted path and remove them
            TrackDao trackDao = new TrackDao();
            List<Track> trackList = trackDao.getActiveByDirectoryInLocation(directory.getId(), path.toAbsolutePath().toString());
            log.info("Path removed, deleting all related tracks (" + trackList.size() + "): " + path);
            for (Track track : trackList) {
                trackDao.delete(track.getId());
            }
        });
    }
    
    /**
     * Get the directory related with the given path.
     * 
     * @param path Path
     */
    private Directory getParentDirectory(Path path) {
        synchronized (watchedDirectoryList) {
            for (Directory directory : watchedDirectoryList) {
                Path directoryPath = Paths.get(directory.getLocation());
                if (path.startsWith(directoryPath)) {
                    return directory;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Cleanup empty albums and artists.
     */
    private void cleanupOrphans() {
        TransactionUtil.handle(() -> {
            // Cleanup empty albums
            AlbumDao albumDao = new AlbumDao();
            albumDao.deleteEmptyAlbum();

            // Delete all artists that don't have any album or track
            ArtistDao artistDao = new ArtistDao();
            artistDao.deleteEmptyArtist();
        });
    }
}
