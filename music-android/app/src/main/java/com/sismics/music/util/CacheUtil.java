package com.sismics.music.util;

import android.os.Environment;
import android.util.Log;

import com.sismics.music.model.Album;
import com.sismics.music.model.PlaylistTrack;
import com.sismics.music.model.Track;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.audio.mp3.MP3FileReader;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ClosedChannelException;
import java.util.ArrayList;
import java.util.List;

/**
 * Cache utilities.
 *
 * @author bgamard.
 */
public class CacheUtil {
    /**
     * Returns the music cache directory.
     * @return Music cache directory
     */
    public static File getMusicCacheDir() {
        File music = Environment.getExternalStoragePublicDirectory("SismicsMusic");
        File cache = new File(music, "cache");
        if (!cache.exists()) {
            cache.mkdirs();
        }
        return cache;
    }

    /**
     * Returns true if a playlistTrack is complete.
     * @param playlistTrack PlaylistTrack
     * @return True if complete
     */
    public static boolean isComplete(PlaylistTrack playlistTrack) {
        return getCompleteCacheFile(playlistTrack).exists();
    }

    /**
     * Returns true if the given track with album is complete.
     * @param album Album associated with the track
     * @param track Track
     * @return True if complete
     */
    public static boolean isComplete(Album album, Track track) {
        PlaylistTrack playlistTrack = new PlaylistTrack(album, track);
        return getCompleteCacheFile(playlistTrack).exists();
    }

    /**
     * Returns the complete playlistTrack file.
     * @param playlistTrack PlaylistTrack
     * @return Complete playlistTrack file
     */
    public static File getCompleteCacheFile(PlaylistTrack playlistTrack) {
        File albumDir = new File(getMusicCacheDir(), playlistTrack.getAlbumId());
        if (!albumDir.exists()) {
            albumDir.mkdirs();
        }
        return new File(albumDir, playlistTrack.getId() + ".complete");
    }

    /**
     * Returns the incomplete playlistTrack file.
     * @param playlistTrack PlaylistTrack
     * @return Incomplete playlistTrack file
     */
    public static File getIncompleteCacheFile(PlaylistTrack playlistTrack) {
        File albumDir = new File(getMusicCacheDir(), playlistTrack.getAlbumId());
        if (!albumDir.exists()) {
            albumDir.mkdirs();
        }
        return new File(albumDir, playlistTrack.getId());
    }

    /**
     * Set a track as complete.
     * @param file File
     * @return True if the operation was successful
     */
    public static boolean setComplete(File file) {
        return file.renameTo(new File(file.getAbsolutePath() + ".complete"));
    }

    /**
     * Returns cached tracks for an album.
     * @param album Album
     * @return Cached tracks
     */
    public static List<Track> getCachedTrack(Album album) {
        List<Track> trackList = new ArrayList<>();
        File albumDir = new File(getMusicCacheDir(), album.getId());
        if (!albumDir.exists()) {
            return trackList;
        }

        // List complete files from this album
        File[] files = albumDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.endsWith(".complete");
            }
        });

        // Extract tags from cached files
        for (File file : files) {
            try {
                Track track = new Track();
                AudioFile audioFile = new MP3FileReader().read(file);
                Tag tag = audioFile.getTag();
                AudioHeader header = audioFile.getAudioHeader();

                track.setLength(header.getTrackLength());
                track.setTitle(tag.getFirst(FieldKey.TITLE));
                track.setId(file.getName().substring(0, file.getName().indexOf(".")));
                trackList.add(track);
            } catch (ClosedChannelException e) {
                // We have been interrupted, don't care
            } catch (Exception e) {
                Log.e("CacheUtil", "Error extracting metadata from file: " + file.getAbsolutePath(), e);
            }
        }

        return trackList;
    }
}
