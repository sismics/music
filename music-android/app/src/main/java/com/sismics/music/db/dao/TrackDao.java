package com.sismics.music.db.dao;

import android.content.Context;

import com.sismics.music.db.Db;
import com.sismics.music.model.Track;
import com.snappydb.DB;
import com.snappydb.SnappydbException;

/**
 * Track DAO.
 *
 * @author jtremeaux
 */
public class TrackDao {

    /**
     * Get a track by ID.
     *
     * @param context The context
     * @param id The ID
     * @return The track
     */
    public static Track getTrackById(Context context, String id) {
        try {
            return Db.db(context).get("track:" + id, Track.class);
        } catch (SnappydbException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns true if the given track is saved.
     *
     * @param trackId Track ID
     * @return True if saved
     */
    public static boolean hasTrack(Context context, String trackId) {
        try {
            return Db.db(context).exists("track:" + trackId);
        } catch (SnappydbException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Update the cache representation of a track.
     * @param context Context
     * @param track Track
     */
    public static void updateTrack(Context context, Track track) {
        try {
            DB snappyDb = Db.db(context);
            if (hasTrack(context, track.getId())) {
                snappyDb.put("track:" + track.getId(), track);
            }
        } catch (SnappydbException e) {
            throw new RuntimeException(e);
        }
    }
}
