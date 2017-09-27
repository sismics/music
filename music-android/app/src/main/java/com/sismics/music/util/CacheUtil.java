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
 * TODO Store albums/artists/tracks in SnappyDB: key = 'album:' + uuid, value = json
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
     * @param artist Artist
     * @param album Album
     * @param track Track
     * @return True if complete
     */
    public static boolean isComplete(Context context, Artist artist, Album album, Track track) {
        PlaylistTrack playlistTrack = new PlaylistTrack(context, artist, album, track);
        return getCompleteCacheFile(context, playlistTrack).exists();
    }

    /**
     * Returns the complete playlistTrack file.
     * @param playlistTrack PlaylistTrack
     * @return Complete playlistTrack file
     */
    public static File getCompleteCacheFile(Context context, PlaylistTrack playlistTrack) {
        File albumDir = new File(getMusicCacheDir(context), playlistTrack.getArtist().getId() + File.separator + playlistTrack.getAlbum().getId());
        if (!albumDir.exists()) {
            albumDir.mkdirs();
        }
        return new File(albumDir, playlistTrack.getTrack().getId() + ".complete");
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
    public static List<Track> getCachedTrack(Context context, Artist artist, Album album) {
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
                String trackId = file.getName().substring(0, file.getName().indexOf("."));
                trackList.add(snappyDb.get("track:" + trackId, Track.class));
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
     * @param artist Artist
     * @param album Album
     * @param track Track
     */
    public static void removeTrack(Context context, Artist artist, Album album, Track track) {
        PlaylistTrack playlistTrack = new PlaylistTrack(context, artist, album, track);
        File file = getCompleteCacheFile(context, playlistTrack);
        if (file.exists()) {
            file.delete();
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
            for (File file : albumDir.listFiles()) {
                file.delete();
            }
            albumDir.delete();
        }
    }
}
