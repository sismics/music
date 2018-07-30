package com.sismics.music.db.dao;

import android.content.Context;

import com.sismics.music.db.Db;
import com.sismics.music.model.Artist;
import com.snappydb.SnappydbException;

/**
 * Artist DAO.
 *
 * @author jtremeaux
 */
public class ArtistDao {
   
    /**
     * Get an artist by ID.
     *
     * @param context The context
     * @param id The ID
     * @return The artist
     */
    public static Artist getArtistById(Context context, String id) {
        try {
            return Db.db(context).get("artist:" + id, Artist.class);
        } catch (SnappydbException e) {
            throw new RuntimeException(e);
        }
    }
}
