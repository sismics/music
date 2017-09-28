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
    public enum CacheStatus {
        NONE, FAILURE, DOWNLOADING, COMPLETE
    }

    private Artist artist;
    private Album album;
    private Track track;

    private CacheStatus cacheStatus;
    private float progress;

    /**
     * Build a new track from server data.
     * @param album Album data
     * @param track Track data
     */
    public PlaylistTrack(Context context, Artist artist, Album album, Track track) {
        if (artist == null || album == null || track == null) {
            throw new IllegalArgumentException("artist, album or track is null");
        }
        this.artist = artist;
        this.track = track;
        this.album = album;
        this.progress = 0;
        cacheStatus = CacheUtil.isTrackCached(context, track.getId()) ? CacheStatus.COMPLETE : CacheStatus.NONE;
    }

    public Artist getArtist() {
        return artist;
    }

    public Album getAlbum() {
        return album;
    }

    public Track getTrack() {
        return track;
    }

    public CacheStatus getCacheStatus() {
        return cacheStatus;
    }

    public void setCacheStatus(CacheStatus cacheStatus) {
        this.cacheStatus = cacheStatus;
    }

    public float getProgress() {
        return progress;
    }

    public void setProgress(float progress) {
        this.progress = progress;
    }
}