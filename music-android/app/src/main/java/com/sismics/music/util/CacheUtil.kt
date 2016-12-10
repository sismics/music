package com.sismics.music.util

import android.os.Environment
import android.util.Log
import com.sismics.music.model.Album
import com.sismics.music.model.PlaylistTrack
import com.sismics.music.model.Track
import org.jaudiotagger.audio.mp3.MP3FileReader
import org.jaudiotagger.tag.FieldKey
import java.io.File
import java.nio.channels.ClosedChannelException
import java.util.*

/**
 * Cache utilities.
 *
 * @author bgamard.
 */
object CacheUtil {
    private val TAG = CacheUtil.javaClass.simpleName

    /**
     * Returns the music cache directory.
     *
     * @return Music cache directory
     */
    val musicCacheDir: File
        get() {
            val music = Environment.getExternalStoragePublicDirectory("SismicsMusic")
            val cache = File(music, "cache")
            if (!cache.exists()) {
                cache.mkdirs()
            }
            return cache
        }

    /**
     * Returns true if a playlistTrack is complete.
     *
     * @param playlistTrack PlaylistTrack
     * @return True if complete
     */
    fun isComplete(playlistTrack: PlaylistTrack): Boolean {
        return getCompleteCacheFile(playlistTrack).exists()
    }

    /**
     * Returns true if the given track with album is complete.
     *
     * @param album Album associated with the track
     * @param track Track
     * @return True if complete
     */
    fun isComplete(album: Album, track: Track): Boolean {
        val playlistTrack = PlaylistTrack(album, track)
        return getCompleteCacheFile(playlistTrack).exists()
    }

    /**
     * Returns the complete playlistTrack file.
     *
     * @param playlistTrack PlaylistTrack
     * @return Complete playlistTrack file
     */
    fun getCompleteCacheFile(playlistTrack: PlaylistTrack): File {
        val albumDir = File(musicCacheDir, playlistTrack.albumId)
        if (!albumDir.exists()) {
            albumDir.mkdirs()
        }
        return File(albumDir, playlistTrack.id + ".complete")
    }

    /**
     * Returns the incomplete playlistTrack file.
     *
     * @param playlistTrack PlaylistTrack
     * @return Incomplete playlistTrack file
     */
    fun getIncompleteCacheFile(playlistTrack: PlaylistTrack): File {
        val albumDir = File(musicCacheDir, playlistTrack.albumId)
        if (!albumDir.exists()) {
            albumDir.mkdirs()
        }
        return File(albumDir, playlistTrack.id)
    }

    /**
     * Set a track as complete.
     *
     * @param file File
     * @return True if the operation was successful
     */
    fun setComplete(file: File): Boolean {
        return file.renameTo(File(file.absolutePath + ".complete"))
    }

    /**
     * Returns cached tracks for an album.
     *
     * @param album Album
     * @return Cached tracks
     */
    fun getCachedTrack(album: Album): List<Track> {
        val trackList = ArrayList<Track>()
        val albumDir = File(musicCacheDir, album.id)
        if (!albumDir.exists()) {
            return trackList
        }

        // List complete files from this album
        val files = albumDir.listFiles { dir, filename -> filename.endsWith(".complete") }

        // Extract tags from cached files
        for (file in files) {
            try {
                val audioFile = MP3FileReader().read(file)
                val tag = audioFile.tag
                val header = audioFile.audioHeader

                val id = file.name.substring(0, file.name.indexOf("."))
                val title = tag?.getFirst(FieldKey.TITLE)
                val track = Track(id, title?:"", header.trackLength)

                trackList.add(track)
            } catch (e: ClosedChannelException) {
                // We have been interrupted, don't care
            } catch (e: Exception) {
                Log.e(TAG, "Error extracting metadata from file: " + file.absolutePath, e)
            }
        }

        return trackList
    }

    /**
     * Returns album IDs containing at least one cached track.
     *
     * @return Albums IDs
     */
    val cachedAlbumSet: Set<String>
        get() {
            val cacheDir = musicCacheDir
            val albumList = cacheDir.listFiles()
            val output = HashSet<String>()
            for (album in albumList) {
                if (album.list { dir, filename -> filename.endsWith(".complete") }.isNotEmpty()) {
                    output.add(album.name)
                }
            }
            return output
        }

    /**
     * Remove a track from the cache.
     *
     * @param album Album
     * @param track Track
     */
    fun removeTrack(album: Album, track: Track) {
        val playlistTrack = PlaylistTrack(album, track)
        val file = getCompleteCacheFile(playlistTrack)
        if (file.exists()) {
            file.delete()
        }
    }

    /**
     * Remove an album from the cache.
     *
     * @param albumId Album ID
     */
    fun removeAlbum(albumId: String) {
        val albumDir = File(musicCacheDir, albumId)
        if (albumDir.exists()) {
            for (file in albumDir.listFiles()) {
                file.delete()
            }
            albumDir.delete()
        }
    }
}
