package com.sismics.music.service

import com.sismics.music.event.PlaylistChangedEvent
import com.sismics.music.model.Album
import com.sismics.music.model.PlaylistTrack
import com.sismics.music.model.Track
import de.greenrobot.event.EventBus
import java.util.*

/**
 * Playlist service.
 *
 * @author bgamard.
 */
object PlaylistService {
    /**
     * Tracks in the playlist.
     */
    private val playlistTrackList = ArrayList<PlaylistTrack>()

    /**
     * Current track index.
     */
    var currentTrackIndex = -1
        private set

    /**
     * Stop the playlist.
     */
    fun stop() {
        currentTrackIndex = -1
        EventBus.getDefault().post(PlaylistChangedEvent())
    }

    /**
     * Returns the next track.
     *
     * @param advance If true, advance the current track accordingly
     * @return Next track
     */
    fun next(advance: Boolean): PlaylistTrack? {
        var index = currentTrackIndex

        if (playlistTrackList.size == 0) {
            return null
        }
        if (index == -1 || index == playlistTrackList.size - 1) {
            // Nothing was played or this is the end, start at the beginning
            index = 0
        } else {
            index++
        }

        if (advance) {
            currentTrackIndex = index
            EventBus.getDefault().post(PlaylistChangedEvent())
        }

        return playlistTrackList[index]
    }

    /**
     * Returns the track after the given one.
     *
     * @param before Previous track
     * @return Next track
     */
    fun after(before: PlaylistTrack): PlaylistTrack? {
        if (!playlistTrackList.contains(before)) {
            // The track has been delete since
            return null
        }

        var beforeIndex = playlistTrackList.indexOf(before)
        return getAt(++beforeIndex)
    }

    /**
     * Change the current played track.
     *
     * @param position Position of the track
     */
    fun change(position: Int) {
        if (getAt(position) != null) {
            currentTrackIndex = position
        } else {
            // The given track index is wrong, reset
            currentTrackIndex = -1
        }
    }

    /**
     * Returns the current track.
     *
     * @return Current track
     */
    fun currentTrack(): PlaylistTrack? {
        if (playlistTrackList.size > currentTrackIndex && currentTrackIndex >= 0) {
            return playlistTrackList[currentTrackIndex]
        }
        return null
    }

    /**
     * Get the track at the given position.
     *
     * @param position PlaylistTrack position
     * @return PlaylistTrack or null if there is no track at this position
     */
    fun getAt(position: Int): PlaylistTrack? {
        if (position > -1 && position < playlistTrackList.size) {
            return playlistTrackList[position]
        }
        return null
    }

    /**
     * Returns the playlist length.
     *
     * @return Playlist length
     */
    fun length(): Int {
        return playlistTrackList.size
    }

    /**
     * Add a track at the end of the playlist.
     *
     * @param album Album
     * @param track Track data
     */
    fun add(album: Album, track: Track) {
        val playlistTrack = PlaylistTrack(album, track)
        playlistTrackList.add(playlistTrack)
        EventBus.getDefault().post(PlaylistChangedEvent())
    }

    /**
     * Clear the playlist.
     */
    fun clear(notify: Boolean) {
        playlistTrackList.clear()
        currentTrackIndex = -1
        if (notify) {
            EventBus.getDefault().post(PlaylistChangedEvent())
        }
    }

    /**
     * Add a list of tracks.
     *
     * @param album Album linked to all tracks
     * @param trackList Tracks
     */
    fun addAll(album: Album, trackList: List<Track>) {
        for (track in trackList) {
            val playlistTrack = PlaylistTrack(album, track)
            playlistTrackList.add(playlistTrack)
        }
        EventBus.getDefault().post(PlaylistChangedEvent())
    }

    /**
     * Remove a track.
     *
     * @param position Track position
     */
    fun remove(position: Int) {
        if (position < currentTrackIndex) {
            currentTrackIndex--
        }
        playlistTrackList.removeAt(position)
        EventBus.getDefault().post(PlaylistChangedEvent())
    }

    /**
     * Move a track.
     *
     * @param oldposition Old position
     * @param position New position
     */
    fun move(oldposition: Int, position: Int) {
        if (oldposition < currentTrackIndex && position >= currentTrackIndex) {
            currentTrackIndex--
        } else if (oldposition > currentTrackIndex && position <= currentTrackIndex) {
            currentTrackIndex++
        } else if (oldposition == currentTrackIndex) {
            currentTrackIndex = position
        }
        playlistTrackList.add(position, playlistTrackList.removeAt(oldposition))
        EventBus.getDefault().post(PlaylistChangedEvent())
    }
}
