package com.sismics.music.core.service.collection;

import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.google.common.io.Files;
import com.google.common.util.concurrent.AbstractScheduledService;
import com.sismics.music.core.dao.dbi.AlbumDao;
import com.sismics.music.core.dao.dbi.ArtistDao;
import com.sismics.music.core.dao.dbi.DirectoryDao;
import com.sismics.music.core.dao.dbi.TrackDao;
import com.sismics.music.core.dao.dbi.criteria.AlbumCriteria;
import com.sismics.music.core.dao.dbi.criteria.TrackCriteria;
import com.sismics.music.core.dao.dbi.dto.AlbumDto;
import com.sismics.music.core.dao.dbi.dto.TrackDto;
import com.sismics.music.core.model.context.AppContext;
import com.sismics.music.core.model.dbi.Album;
import com.sismics.music.core.model.dbi.Artist;
import com.sismics.music.core.model.dbi.Directory;
import com.sismics.music.core.model.dbi.Track;
import com.sismics.music.core.service.albumart.AlbumArtImporter;
import com.sismics.music.core.util.DirectoryNameParser;
import com.sismics.music.core.util.TransactionUtil;
import org.apache.commons.lang.StringUtils;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Collection service.
 *
 * @author jtremeaux
 */
public class CollectionService extends AbstractScheduledService {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(CollectionService.class);

    public CollectionService() {
    }

    @Override
    protected void startUp() {
    }

    @Override
    protected void shutDown() {
    }

    @Override
    protected void runOneIteration() throws Exception {
        TransactionUtil.handle(() -> reindex());
    }

    @Override
    protected Scheduler scheduler() {
        return Scheduler.newFixedDelaySchedule(0, 24, TimeUnit.HOURS);
    }

    /**
     * Add a directory to the index / update existing index.
     *
     * @param directory Directory to index
     */
    public void addDirectoryToIndex(Directory directory) {
        if (log.isInfoEnabled()) {
            log.info(MessageFormat.format("Adding directory {0} to index", directory.getLocation()));
        }
        // Index the directory recursively
        CollectionVisitor collectionVisitor = new CollectionVisitor(directory);
        collectionVisitor.index();
        
        // Delete non-existing tracks
        Set<String> existingFileNameSet = collectionVisitor.getFileNameSet();
        TrackDao trackDao = new TrackDao();
        List<TrackDto> trackDtoList = trackDao.findByCriteria(new TrackCriteria().setDirectoryId(directory.getId()));
        for (TrackDto trackDto : trackDtoList) {
            if (!existingFileNameSet.contains(trackDto.getFileName())) {
                trackDao.delete(trackDto.getId());
            }
        }
        
        // Cleanup empty albums
        AlbumDao albumDao = new AlbumDao();
        albumDao.deleteEmptyAlbum();
        
        // Delete all artists that don't have any album or track
        ArtistDao artistDao = new ArtistDao();
        artistDao.deleteEmptyArtist();

        if (log.isInfoEnabled()) {
            log.info(MessageFormat.format("Done adding directory {0} to index", directory.getLocation()));
        }
    }

    /**
     * Remove a directory from the index.
     *
     * @param directory Directory to index
     */
    public void removeDirectoryFromIndex(Directory directory) {
        if (log.isInfoEnabled()) {
            log.info(MessageFormat.format("Removing directory {0} from index", directory.getLocation()));
        }
        
        // Search all tracks included in the deleted path and remove them
        TrackDao trackDao = new TrackDao();
        List<Track> trackList = trackDao.getActiveByDirectoryInLocation(directory.getId(), directory.getLocation());
        for (Track track : trackList) {
            trackDao.delete(track.getId());
        }
        
        // Delete all albums from this directory
        AlbumDao albumDao = new AlbumDao();
        List<AlbumDto> albumList = albumDao.findByCriteria(new AlbumCriteria().setDirectoryId(directory.getId()));
        for (AlbumDto albumDto : albumList) {
            albumDao.delete(albumDto.getId());
        }
        
        // Delete all artists that don't have any album or track
        ArtistDao artistDao = new ArtistDao();
        artistDao.deleteEmptyArtist();

        if (log.isInfoEnabled()) {
            log.info(MessageFormat.format("Done removing directory {0} from index", directory.getLocation()));
        }
    }

    /**
     * Add / update a media file to the index.
     *
     * @param directory Directory to index
     * @param file File to add
     */
    public void indexFile(Directory directory, Path file) {
        Stopwatch stopWatch = Stopwatch.createStarted();
        // TODO This method should handle albumarts too
        try {
            TrackDao trackDao = new TrackDao();
            Track track = trackDao.getActiveByDirectoryAndFilename(directory.getId(), file.toAbsolutePath().toString());
            if (track != null) {
                readTrackMetadata(directory, file, track);
                trackDao.update(track);

                // FIXME update album date if track has changed?
            } else {
                track = new Track();
                track.setFileName(file.toAbsolutePath().toString());

                readTrackMetadata(directory, file, track);
                trackDao.create(track);

                // Update the album date
                // TODO This makes no sense
                /*Album album = new Album(track.getAlbumId());
                album.setUpdateDate(track.getCreateDate());
                AlbumDao albumDao = new AlbumDao();
                albumDao.updateAlbumDate(album);*/
            }
        } catch (Exception e) {
            log.error("Error extracting metadata from file: " + file, e);
        }
        if (log.isInfoEnabled()) {
            log.info(MessageFormat.format("File {0} indexed in {1}", file, stopWatch));
        }
    }
    
    /**
     * Read metadata from a media file into the Track.
     *
     * @param rootDirectory Root directory to index
     * @param file Media file to read from
     * @param track Track entity (updated)
     */
    public void readTrackMetadata(Directory rootDirectory, Path file, Track track) throws Exception {
        Path parentPath = file.getParent();
        DirectoryNameParser nameParser = new DirectoryNameParser(parentPath);
        String albumArtistName = StringUtils.abbreviate(nameParser.getArtistName(), 1000).trim();
        String albumName = StringUtils.abbreviate(nameParser.getAlbumName(), 1000).trim();
        ArtistDao artistDao = new ArtistDao();
        
        AudioFile audioFile = AudioFileIO.read(file.toFile());
        Tag tag = audioFile.getTag();
        
        // The album artist can't be null, check is in the directory name parser
        Artist albumArtist = artistDao.getActiveByName(albumArtistName);
        if (albumArtist == null) {
            albumArtist = new Artist();
            albumArtist.setName(albumArtistName);
            artistDao.create(albumArtist);
        }
        
        if (tag == null) {
            // No tag available, use filename as title and album artist as artist, and guess the rest
            track.setTitle(Files.getNameWithoutExtension(file.getFileName().toString()));
            track.setArtistId(albumArtist.getId());
            track.setLength((int) (file.toFile().length() / 128000));
            track.setBitrate(128);
            track.setFormat(Files.getFileExtension(file.getFileName().toString()));
            track.setVbr(true);
        } else {
            AudioHeader header = audioFile.getAudioHeader();
    
            track.setLength(header.getTrackLength());
            track.setBitrate(header.getSampleRateAsNumber());
            track.setFormat(StringUtils.abbreviate(header.getEncodingType(), 50));
            track.setVbr(header.isVariableBitRate());
    
            String order = tag.getFirst(FieldKey.TRACK);
            if (!Strings.isNullOrEmpty(order)) {
                try {
                    track.setOrder(Integer.valueOf(order));
                } catch (NumberFormatException e) {
                    // Ignore parsing errors
                }
            }
    
            String year = tag.getFirst(FieldKey.YEAR);
            if (!Strings.isNullOrEmpty(year)) {
                try {
                    track.setYear(Integer.valueOf(year));
                } catch (NumberFormatException e) {
                    // Ignore parsing errors
                }
            }
    
            // Track title (can be empty string)
            track.setTitle(StringUtils.abbreviate(tag.getFirst(FieldKey.TITLE), 2000).trim());
            
            // Track artist (can be empty string)
            String artistName = StringUtils.abbreviate(tag.getFirst(FieldKey.ARTIST), 1000).trim();
            Artist artist = artistDao.getActiveByName(artistName);
            if (artist == null) {
                artist = new Artist();
                artist.setName(artistName);
                artistDao.create(artist);
            }
            track.setArtistId(artist.getId());
        }

        // Track album
        AlbumDao albumDao = new AlbumDao();
        Album album = albumDao.getActiveByArtistIdAndName(albumArtist.getId(), albumName);
        if (album == null) {
            // Import album art
            AlbumArtImporter albumArtImporter = new AlbumArtImporter();
            File albumArtFile = albumArtImporter.scanDirectory(file.getParent());

            album = new Album();
            album.setArtistId(albumArtist.getId());
            album.setDirectoryId(rootDirectory.getId());
            album.setName(albumName);
            album.setLocation(file.getParent().toString());
            if (albumArtFile != null) {
                // TODO Remove this, albumarts are scanned separately
                AppContext.getInstance().getAlbumArtService().importAlbumArt(album, albumArtFile, false);
            }
            Date updateDate = getDirectoryUpdateDate(parentPath);
            album.setCreateDate(updateDate);
            album.setUpdateDate(updateDate);
            albumDao.create(album);
        }
        track.setAlbumId(album.getId());
    }

    private Date getDirectoryUpdateDate(Path path) {
        try {
            return new Date(java.nio.file.Files.getLastModifiedTime(path).toMillis());
        } catch (Exception e) {
            log.error(MessageFormat.format("Cannot read date from directory {0}", path));
        }
        return null;
    }

    /**
     * Reindex the whole collection.
     */
    public void reindex() {
        DirectoryDao directoryDao = new DirectoryDao();
        List<Directory> directoryList = directoryDao.findAllEnabled();
        for (Directory directory : directoryList) {
            addDirectoryToIndex(directory);
        }
    }

    /**
     * Update the album scores.
     * TODO implement a more elaborated scoring function
     */
    public void updateScore() {
//        AlbumDao albumDao = new AlbumDao();
//        List<AlbumDto> albumList = albumDao.findByCriteria(new AlbumCriteria());
//        for (AlbumDto albumDto : albumList) {
//            Integer score = albumDao.getFavoriteCountByAlbum(albumDto.getId());
//
//            Album album = new Album();
//            album.setId(albumDto.getId());
//            album.setScore(score);
//
//            albumDao.updateScore(album);
//        }
    }
}
