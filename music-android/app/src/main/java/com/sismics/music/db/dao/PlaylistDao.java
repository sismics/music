package com.sismics.music.db.dao;

import android.content.Context;

import com.sismics.music.db.Db;
import com.snappydb.SnappydbException;

import java.util.ArrayList;
import java.util.List;

/**
 * Playlist DAO.
 *
 * @author jtremeaux
 */
public class PlaylistDao {
    public static class PlaylistItem {
        public String trackId;

        public String albumId;

        public String artistId;

        public PlaylistItem() {
        }

        public PlaylistItem(String trackId, String albumId, String artistId) {
            this.trackId = trackId;
            this.albumId = albumId;
            this.artistId = artistId;
        }
    }
    /**
     * Save the current playlist.
     *
     * @param context The context
     * @param playlistItemList The playlist items
     */
    public static void savePlaylist(Context context, List<PlaylistItem> playlistItemList) {
        try {
            Db.db(context).put("playlist", playlistItemList);
        } catch (SnappydbException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns saved playlist.
     *
     * @return The playlist items
     */
    public static List<PlaylistItem> getPlaylist(Context context) {
        try {
            List<PlaylistItem> playlistItem = Db.db(context).getObject("playlist", ArrayList.class);
            if (playlistItem != null) {
                return playlistItem;
            } else {
                return new ArrayList<>();
            }
        } catch (SnappydbException e) {
            throw new RuntimeException(e);
        }
    }
}
