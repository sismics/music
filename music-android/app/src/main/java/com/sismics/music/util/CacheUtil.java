package com.sismics.music.util;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.sismics.music.model.Album;
import com.sismics.music.model.PlaylistTrack;
import com.sismics.music.model.Track;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.audio.mp3.MP3FileReader;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import java.io.File;
import java.nio.channels.ClosedChannelException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Cache utilities.
 *
 * TODO Store albums/artists/tracks in SnappyDB: key = 'album:' + uuid, value = json
 * TODO Add a level to the filesystem cache: artist>album>track
 * TODO Remove CACHED_ALBUMS_LIST_JSON from the shared preferences
 * TODO Index album/artist/track names in SnappyDB: key = 'albumname:' + uuid, value = name
 * TODO Clear SnappyDB entries when unpinning tracks (and delete empty albums/artists)
 *
 * @author bgamard.
 */
public class CacheUtil {
    /**
     * Returns the music cache directory.
     * @return Music cache directory
     */
    public static File getMusicCacheDir(Context context) {
        File cacheDir = ContextCompat.getExternalCacheDirs(context)[0];
        File cache = new File(cacheDir, "music");
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
    public static boolean isComplete(Context context, PlaylistTrack playlistTrack) {
        return getCompleteCacheFile(context, playlistTrack).exists();
    }

    /**
     * Returns true if the given track with album is complete.
     * @param album Album associated with the track
     * @param track Track
     * @return True if complete
     */
    public static boolean isComplete(Context context, Album album, Track track) {
        PlaylistTrack playlistTrack = new PlaylistTrack(context, album, track);
        return getCompleteCacheFile(context, playlistTrack).exists();
    }

    /**
     * Returns the complete playlistTrack file.
     * @param playlistTrack PlaylistTrack
     * @return Complete playlistTrack file
     */
    public static File getCompleteCacheFile(Context context, PlaylistTrack playlistTrack) {
        File albumDir = new File(getMusicCacheDir(context), playlistTrack.getAlbumId());
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
    public static File getIncompleteCacheFile(Context context, PlaylistTrack playlistTrack) {
        File albumDir = new File(getMusicCacheDir(context), playlistTrack.getAlbumId());
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
    public static List<Track> getCachedTrack(Context context, Album album) {
        List<Track> trackList = new ArrayList<>();
        File albumDir = new File(getMusicCacheDir(context), album.getId());
        if (!albumDir.exists()) {
            return trackList;
        }

        // List complete files from this album
        File[] files = albumDir.listFiles((dir, filename) -> filename.endsWith(".complete"));

        // Extract tags from cached files
        for (File file : files) {
            try {
                Track track = new Track();
                AudioFile audioFile = new MP3FileReader().read(file);
                Tag tag = audioFile.getTag();
                AudioHeader header = audioFile.getAudioHeader();

                track.setLength(header.getTrackLength());
                if (tag != null) {
                    track.setTitle(tag.getFirst(FieldKey.TITLE));
                }
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

    /**
     * Returns album IDs containing at least one cached track.
     * @return Albums IDs
     */
    public static Set<String> getCachedAlbumSet(Context context) {
        File cacheDir = getMusicCacheDir(context);
        File[] albumList = cacheDir.listFiles();
        Set<String> output = new HashSet<>();
        for (File album : albumList) {
            if (album.list((dir, filename) -> filename.endsWith(".complete")).length > 0) {
                output.add(album.getName());
            }
        }
        return output;
    }

    /**
     * Remove a track from the cache.
     * @param album Album
     * @param track Track
     */
    public static void removeTrack(Context context, Album album, Track track) {
        PlaylistTrack playlistTrack = new PlaylistTrack(context, album, track);
        File file = getCompleteCacheFile(context, playlistTrack);
        if (file.exists()) {
            file.delete();
        }
    }

    /**
     * Remove an album from the cache.
     * @param albumId Album ID
     */
    public static void removeAlbum(Context context, String albumId) {
        File albumDir = new File(getMusicCacheDir(context), albumId);
        if (albumDir.exists()) {
            for (File file : albumDir.listFiles()) {
                file.delete();
            }
            albumDir.delete();
        }
    }
}
