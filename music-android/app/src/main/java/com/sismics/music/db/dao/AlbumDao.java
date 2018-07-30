package com.sismics.music.db.dao;

import android.content.Context;

import com.sismics.music.db.Db;
import com.sismics.music.model.Album;
import com.snappydb.SnappydbException;

/**
 * Album DAO.
 *
 * @author jtremeaux
 */
public class AlbumDao {

    /**
     * Get an album by ID.
     *
     * @param context The context
     * @param id The ID
     * @return The album
     */
    public static Album getAlbumById(Context context, String id) {
        try {
            return Db.db(context).get("album:" + id, Album.class);
        } catch (SnappydbException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns true if the given track is saved.
     *
     * @param albumId Album ID
     * @return True if saved
     */
    public static boolean hasAlbum(Context context, String albumId) {
        try {
            return Db.db(context).exists("album:" + albumId);
        } catch (SnappydbException e) {
            throw new RuntimeException(e);
        }
    }
}
