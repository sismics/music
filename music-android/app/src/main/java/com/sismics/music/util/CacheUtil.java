package com.sismics.music.util;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.sismics.music.model.Album;
import com.sismics.music.model.Artist;
import com.sismics.music.model.FullAlbum;
import com.sismics.music.model.PlaylistTrack;
import com.sismics.music.model.Track;
import com.snappydb.DB;
import com.snappydb.SnappyDB;
import com.snappydb.SnappydbException;

import java.io.File;
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
    public static File getMusicCacheDir(Context context) {
        File cacheDir = ContextCompat.getExternalCacheDirs(context)[0];
        File cache = new File(cacheDir, "music");
        if (!cache.exists()) {
            cache.mkdirs();
        }
        return cache;
    }

    /**
     * Returns true if the given track is complete.
     * @param trackId Track ID
     * @return True if complete
     */
    public static boolean isTrackCached(Context context, String trackId) {
        try {
            return SnappyDB.with(context).exists("track:" + trackId);
        } catch (SnappydbException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns true if the given track is complete.
     * @param albumId Album ID
     * @return True if complete
     */
    public static boolean isAlbumCached(Context context, String albumId) {
        try {
            return SnappyDB.with(context).exists("album:" + albumId);
        } catch (SnappydbException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the complete playlistTrack file.
     * @param playlistTrack PlaylistTrack
     * @return Complete playlistTrack file
     */
    public static File getCompleteCacheFile(Context context, PlaylistTrack playlistTrack) {
        return new File(getMusicCacheDir(context), playlistTrack.getArtist().getId()
                + File.separator + playlistTrack.getAlbum().getId()
                + File.separator + playlistTrack.getTrack().getId() + ".complete");
    }

    public static File getCompleteCacheFile(Context context, String artistId, String albumId, String trackId) {
        return new File(getMusicCacheDir(context), artistId
                + File.separator + albumId
                + File.separator + trackId + ".complete");
    }

    /**
     * Returns the incomplete playlistTrack file.
     * @param playlistTrack PlaylistTrack
     * @return Incomplete playlistTrack file
     */
    public static File getIncompleteCacheFile(Context context, PlaylistTrack playlistTrack) {
        File albumDir = new File(getMusicCacheDir(context), playlistTrack.getArtist().getId() + File.separator + playlistTrack.getAlbum().getId());
        if (!albumDir.exists()) {
            albumDir.mkdirs();
        }
        return new File(albumDir, playlistTrack.getTrack().getId());
    }

    /**
     * Set a track as complete.
     * @param context Context
     * @param playlistTrack PlaylistTrack
     * @param file File
     * @return True if the operation was successful
     */
    public static boolean setComplete(Context context, PlaylistTrack playlistTrack, File file) {
        try {
            DB snappyDb = SnappyDB.with(context);
            snappyDb.put("album:" + playlistTrack.getAlbum().getId(), playlistTrack.getAlbum());
            snappyDb.put("artist:" + playlistTrack.getArtist().getId(), playlistTrack.getArtist());
            snappyDb.put("track:" + playlistTrack.getTrack().getId(), playlistTrack.getTrack());
        } catch (SnappydbException e) {
            throw new RuntimeException(e);
        }
        return file.renameTo(new File(file.getAbsolutePath() + ".complete"));
    }

    /**
     * Returns cached tracks for an album.
     * @param album Album
     * @return Cached tracks
     */
    public static List<Track> getCachedTrackList(Context context, Artist artist, Album album) {
        List<Track> trackList = new ArrayList<>();
        File albumDir = new File(getMusicCacheDir(context), artist.getId() + File.separator + album.getId());
        if (!albumDir.exists()) {
            return trackList;
        }

        // List complete files from this album
        File[] files = albumDir.listFiles((dir, filename) -> filename.endsWith(".complete"));

        // Get tracks from cache
        try {
            DB snappyDb = SnappyDB.with(context);
            for (File file : files) {
                String trackKey = "track:" + file.getName().substring(0, file.getName().indexOf("."));
                if (snappyDb.exists(trackKey)) {
                    trackList.add(snappyDb.get(trackKey, Track.class));
                }
            }
        } catch (SnappydbException e) {
            Log.e("CacheUtil", "Error getting tracks from cache", e);
        }

        return trackList;
    }

    /**
     * Returns cached albums.
     * @return Albums
     */
    public static List<FullAlbum> getCachedAlbumList(Context context) {
        List<FullAlbum> albumList = new ArrayList<>();
        try {
            DB snappyDb = SnappyDB.with(context);
            for (String albumKey : SnappyDB.with(context).findKeys("album:")) {
                Album album = snappyDb.get(albumKey, Album.class);
                Artist artist = snappyDb.get("artist:" + album.getArtistId(), Artist.class);
                albumList.add(new FullAlbum(artist, album));
            }
        } catch (SnappydbException e) {
            throw new RuntimeException(e);
        }
        return albumList;
    }

    /**
     * Remove a track from the cache.
     * @param artistId Artist ID
     * @param albumId Album ID
     * @param trackId Track ID
     */
    public static void removeTrack(Context context, String artistId, String albumId, String trackId) {
        File file = getCompleteCacheFile(context, artistId, albumId, trackId);
        if (file.exists()) {
            file.delete();
        }

        try {
            DB snappyDb = SnappyDB.with(context);
            String trackKey = "track:" + trackId;
            String albumKey = "album:" + albumId;
            String artistKey = "artist:" + artistId;
            if (snappyDb.exists(trackKey)) {
                snappyDb.del(trackKey);
            }

            File albumDir = new File(getMusicCacheDir(context), artistId + File.separator + albumId);
            if (albumDir.exists()) {
                File[] trackFileList = albumDir.listFiles();
                boolean empty = true;
                for (File trackFile : trackFileList) {
                    if (trackFile.getName().endsWith(".complete")) {
                        empty = false;
                    } else {
                        trackFile.delete();
                    }
                }

                if (empty) {
                    albumDir.delete();
                    if (snappyDb.exists(albumKey)) {
                        snappyDb.del(albumKey);
                    }
                }
            }

            File artistDir = new File(getMusicCacheDir(context), artistId);
            if (artistDir.exists() && artistDir.list().length == 0) {
                artistDir.delete();
                if (snappyDb.exists(artistKey)) {
                    snappyDb.del(artistKey);
                }
            }
        } catch (SnappydbException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Remove an album from the cache.
     * @param artistId Artist ID
     * @param albumId Album ID
     */
    public static void removeAlbum(Context context, String artistId, String albumId) {
        File albumDir = new File(getMusicCacheDir(context), artistId + File.separator + albumId);
        if (albumDir.exists()) {
            File[] fileList = albumDir.listFiles((dir, filename) -> filename.endsWith(".complete"));
            for (File file : fileList) {
                String trackId = file.getName().substring(0, file.getName().indexOf("."));
                removeTrack(context, artistId, albumId, trackId);
            }
        }
    }
}
