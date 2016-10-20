package com.sismics.music.event

import com.sismics.music.model.Album
import com.sismics.music.model.PlaylistTrack
import com.sismics.music.service.MusicService

/**
 * Open album event.
 *
 * @author bgamard
 */
class AlbumOpenedEvent(val album: Album)

/**
 * Media player seeking event.
 *
 * @author bgamard.
 */
class MediaPlayerSeekEvent(val position: Int)

/**
 * Media player state changed event.
 *
 * @author bgamard.
 */
class MediaPlayerStateChangedEvent(val state: MusicService.State,
                                   val songStartedAt: Long,
                                   val playlistTrack: PlaylistTrack?,
                                   val currentPosition: Int,
                                   val duration: Int)

/**
 * My music fragment menu visibility event.
 *
 * @author bgamard.
 */
class MyMusicMenuVisibilityChangedEvent(val isMenuVisible: Boolean)

/**
 * Offline mode changed event.
 *
 * @author bgamard.
 */
class OfflineModeChangedEvent(val isOfflineMode: Boolean)

/**
 * Playlist changed event.
 *
 * @author bgamard
 */
class PlaylistChangedEvent

/**
 * Track cache status changed.
 * This event can specify an track or not.

 * @author bgamard.
 */
class TrackCacheStatusChangedEvent(val playlistTrack: PlaylistTrack?)