package com.sismics.music.core.service.collection;

import com.google.common.base.Strings;
import com.google.common.util.concurrent.AbstractScheduledService;
import com.sismics.music.core.dao.dbi.AlbumDao;
import com.sismics.music.core.dao.dbi.ArtistDao;
import com.sismics.music.core.dao.dbi.DirectoryDao;
import com.sismics.music.core.dao.dbi.TrackDao;
import com.sismics.music.core.dao.dbi.criteria.AlbumCriteria;
import com.sismics.music.core.dao.dbi.dto.AlbumDto;
import com.sismics.music.core.model.context.AppContext;
import com.sismics.music.core.model.dbi.Album;
import com.sismics.music.core.model.dbi.Artist;
import com.sismics.music.core.model.dbi.Directory;
import com.sismics.music.core.model.dbi.Track;
import com.sismics.music.core.service.albumart.AlbumArtImporter;
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
import java.util.List;
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
        TransactionUtil.handle(new Runnable() {
            @Override
            public void run() {
                // NOP
            }
        });
    }

    @Override
    protected Scheduler scheduler() {
        return Scheduler.newFixedDelaySchedule(0, 1, TimeUnit.DAYS);
    }

    /**
     * Add a directory to the index / update existing index.
     *
     * @param directory Directory to index
     */
    public void addDirectoryToIndex(Directory directory) {
        // Add / update this directory to the index
        File file = new File(directory.getLocation());
        if (!file.exists() || !file.isDirectory()) {
            log.error("Cannot read from directory: " + file.getAbsolutePath());
            return;
        }
        indexDirectory(directory, file);

        // Delete all artists that don't have any album
        ArtistDao artistDao = new ArtistDao();
        artistDao.deleteEmptyArtist(directory.getId());
    }

    /**
     * Remove a directory from the index.
     *
     * @param directory Directory to index
     */
    public void removeDirectoryFromIndex(Directory directory) {
        // Delete all albums from this directory
        AlbumDao albumDao = new AlbumDao();
        List<AlbumDto> albumList = albumDao.findByCriteria(new AlbumCriteria().setDirectoryId(directory.getId()));
        for (AlbumDto albumDto : albumList) {
            albumDao.delete(albumDto.getId());
        }

        // Delete all artists that don't have any album
        ArtistDao artistDao = new ArtistDao();
        artistDao.deleteEmptyArtist(directory.getId());
    }

    /**
     * Index a directory recursively.
     *
     * @param rootDirectory Directory to index
     * @param parentDirectory Directory on the FS
     */
    private void indexDirectory(Directory rootDirectory, File parentDirectory) {
        for (File fileEntry : parentDirectory.listFiles()) {
            if (fileEntry.isDirectory()) {
                indexDirectory(rootDirectory, fileEntry);
            } else {
                // TODO filter media files properly
                if (fileEntry.getName().endsWith(".mp3")) {
                    indexFile(rootDirectory, fileEntry);
                }
            }
        }
    }

    /**
     * Add / update a media file to the index.
     *
     * @param rootDirectory Directory to index
     * @param file File to add
     */
    private void indexFile(Directory rootDirectory, File file) {
        TrackDao trackDao = new TrackDao();
        Track track = trackDao.getActiveByDirectoryAndFilename(rootDirectory.getId(), file.getAbsolutePath());
        if (track != null) {
            readTrackMetadata(rootDirectory, file, track);
        } else {
            track = new Track();
            track.setFileName(file.getAbsolutePath());

            readTrackMetadata(rootDirectory, file, track);
            trackDao.create(track);
        }
    }

    /**
     * Read metadata from a media file into the Track.
     *
     * @param rootDirectory Root directory to index
     * @param file Media file to read from
     * @param track Track entity (updated)
     */
    private void readTrackMetadata(Directory rootDirectory, File file, Track track) {
        try {
            AudioFile audioFile = AudioFileIO.read(file);
            Tag tag = audioFile.getTag();
            // TODO deal with empty tags
            AudioHeader header = audioFile.getAudioHeader();

            track.setLength(header.getTrackLength());
            track.setBitrate(header.getSampleRateAsNumber());
            track.setFormat(header.getEncodingType());
            track.setVbr(header.isVariableBitRate());

            String year = tag.getFirst(FieldKey.YEAR);
            if (!Strings.isNullOrEmpty(year)) {
                try {
                    track.setYear(Integer.valueOf(year));
                } catch (NumberFormatException e) {
                    // Ignore parsing errors
                }
            }
            
            track.setTitle(StringUtils.abbreviate(tag.getFirst(FieldKey.TITLE), 2000));
            String artistName = StringUtils.abbreviate(tag.getFirst(FieldKey.ARTIST), 1000);
            ArtistDao artistDao = new ArtistDao();
            Artist artist = artistDao.getActiveByName(artistName);
            if (artist == null) {
                artist = new Artist();
                artist.setName(artistName);
                artistDao.create(artist);
            }
            track.setArtistId(artist.getId());

            String albumArtistName = StringUtils.abbreviate(tag.getFirst(FieldKey.ALBUM_ARTIST), 1000);
            Artist albumArtist = null;
            if (!Strings.isNullOrEmpty(albumArtistName)) {
                albumArtist = artistDao.getActiveByName(albumArtistName);
                if (albumArtist == null) {
                    albumArtist = new Artist();
                    albumArtist.setName(albumArtistName);
                    artistDao.create(albumArtist);
                }
            } else {
                albumArtist = artist;
            }

            String albumName = StringUtils.abbreviate(tag.getFirst(FieldKey.ALBUM), 1000);
            AlbumDao albumDao = new AlbumDao();
            Album album = albumDao.getActiveByArtistIdAndName(albumArtist.getId(), albumName);
            if (album == null) {
                // Import album art
                AlbumArtImporter albumArtImporter = new AlbumArtImporter();
                File albumArtFile = albumArtImporter.scanDirectory(file.getParentFile());

                album = new Album();
                album.setArtistId(albumArtist.getId());
                album.setDirectoryId(rootDirectory.getId());
                album.setName(albumName);
                if (albumArtFile != null) {
                    String albumArtId = AppContext.getInstance().getAlbumArtService().importAlbumArt(albumArtFile);
                    album.setAlbumArt(albumArtId);
                }
                albumDao.create(album);
            }
            track.setAlbumId(album.getId());
        } catch (Exception e) {
            log.error("Error extracting metadata from file: " + file.getAbsolutePath(), e);
        }
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
}
