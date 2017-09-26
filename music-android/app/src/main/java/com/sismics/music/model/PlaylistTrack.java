package com.sismics.music.model;

import android.content.Context;

import com.sismics.music.util.CacheUtil;

/**
 * A track from the playlist.
 */
public class PlaylistTrack {
    /**
     * Cache status.
     */
    public static enum CacheStatus {
        NONE, DOWNLOADING, COMPLETE
    }

    /**
     * Track data.
     */
    private Track track;

    /**
     * Album data.
     */
    private Album album;

    /**
     * Cache status.
     */
    private CacheStatus cacheStatus;

    /**
     * Build a new track from server data.
     * @param album Album data
     * @param track Track data
     */
    public PlaylistTrack(Context context, Album album, Track track) {
        this.track = track;
        this.album = album;
        cacheStatus = CacheUtil.isComplete(context, this) ? CacheStatus.COMPLETE : CacheStatus.NONE;
    }

    /**
     * Returns the track title.
     * @return Track title
     */
    public String getTitle() {
        return track.getTitle();
    }

    /**
     * Returns the track ID.
     * @return Track ID
     */
    public String getId() {
        return track.getId();
    }

    /**
     * Returns the track artist name.
     * @return Track artist name
     */
    public String getArtistName() {
        return album.getArtistName();
    }

    /**
     * Returns the track album ID.
     * @return Track album ID
     */
    public String getAlbumId() {
        return album.getId();
    }

    /**
     * Returns the track album name.
     * @return Track album name
     */
    public String getAlbumName() {
        return album.getName();
    }

    /**
     * Returns the track length.
     * @return Track length
     */
    public long getLength() {
        return track.getLength();
    }

    /**
     * Returns the track cache status.
     * @return Track cache status
     */
    public CacheStatus getCacheStatus() {
        return cacheStatus;
    }

    /**
     * Set the track cache status.
     * @param cacheStatus New cache status
     */
    public void setCacheStatus(CacheStatus cacheStatus) {
        this.cacheStatus = cacheStatus;
    }
}