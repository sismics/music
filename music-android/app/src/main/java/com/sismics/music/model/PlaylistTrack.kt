package com.sismics.music.model

import com.sismics.music.util.CacheUtil

/**
 * A track from the playlist.
 */
class PlaylistTrack
/**
 * Build a new track from server data.
 * @param album Album data
 * *
 * @param track Track data
 */
(
        /**
         * Album data.
         */
        private val album: Album,
        /**
         * Track data.
         */
        private val track: Track) {
    /**
     * Cache status.
     */
    enum class CacheStatus {
        NONE, DOWNLOADING, COMPLETE
    }

    /**
     * Cache status.
     */
    /**
     * Returns the track cache status.
     * @return Track cache status
     */
    /**
     * Set the track cache status.
     * @param cacheStatus New cache status
     */
    var cacheStatus: CacheStatus = if (CacheUtil.isComplete(this)) CacheStatus.COMPLETE else CacheStatus.NONE

    /**
     * Returns the track title.
     * @return Track title
     */
    val title: String
        get() = track.title

    /**
     * Returns the track ID.
     * @return Track ID
     */
    val id: String
        get() = track.id

    /**
     * Returns the track artist name.
     * @return Track artist name
     */
    val artistName: String
        get() = album.artistName!!

    /**
     * Returns the track album ID.
     * @return Track album ID
     */
    val albumId: String
        get() = album.id!!

    /**
     * Returns the track album name.
     * @return Track album name
     */
    val albumName: String
        get() = album.name!!

    /**
     * Returns the track length.
     * @return Track length
     */
    val length: Long
        get() = track.length.toLong()
}