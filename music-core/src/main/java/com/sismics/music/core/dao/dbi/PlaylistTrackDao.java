package com.sismics.music.core.dao.dbi;

import com.sismics.music.core.model.dbi.PlaylistTrack;
import com.sismics.util.context.ThreadLocalContext;
import com.sismics.util.dbi.ObjectMapper;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.util.StringMapper;

/**
 * Playlist track DAO.
 * 
 * @author jtremeaux
 */
public class PlaylistTrackDao {
    /**
     * Creates a new playlist track.
     *
     * @param playlistTrack Playlist track to create
     * @return Playlist track ID
     */
    public String create(PlaylistTrack playlistTrack) {
        final Handle handle = ThreadLocalContext.get().getHandle();
        handle.createStatement("insert into " +
                "  t_playlist_track (id, playlist_id, track_id, number)" +
                "  values(:id, :playlistId, :trackId, :number)")
                .bind("id", playlistTrack.getId())
                .bind("playlistId", playlistTrack.getPlaylistId())
                .bind("trackId", playlistTrack.getTrackId())
                .bind("number", playlistTrack.getOrder())
                .execute();

        return playlistTrack.getId();
    }

    /**
     * Returns a playlist by playlist ID.
     *
     * @param playlistId Playlist ID
     */
    public void deleteByPlaylistId(String playlistId) {
        final Handle handle = ThreadLocalContext.get().getHandle();
        handle.createStatement("delete from t_playlist_track pt" +
                "  where pt.playlist_id = :playlistId ")
                .bind("playlistId", playlistId)
                .execute();
    }

    /**
     * Returns the last order in the playlist, or 0 if the playlist is empty.
     *
     * @param playlistId Playlist ID
     */
    public Integer getPlaylistTrackNextOrder(String playlistId) {
        final Handle handle = ThreadLocalContext.get().getHandle();
        Object number = handle.createQuery("select max(pt.number) from t_playlist_track pt where pt.playlist_id = :playlistId")
                .bind("playlistId", playlistId)
                .map(ObjectMapper.FIRST)
                .first();
        return number == null ? 0 : ((Integer) number) + 1;
    }

    /**
     * Insert a track at the given position.
     *
     * @param playlistId Playlist ID
     * @param trackId ID of the track to insert
     * @param number Position to insert
     */
    public void insertPlaylistTrack(String playlistId, String trackId, Integer number) {
        // Reorder currrent tracks
        final Handle handle = ThreadLocalContext.get().getHandle();
        handle.createStatement("update t_playlist_track pt set pt.number = pt.number + 1 where pt.playlist_id = :playlistId and pt.number >= :number")
                .bind("playlistId", playlistId)
                .bind("number", number)
                .execute();

        // Insert new track
        PlaylistTrack playlistTrack = new PlaylistTrack();
        playlistTrack.setPlaylistId(playlistId);
        playlistTrack.setTrackId(trackId);
        playlistTrack.setOrder(number);
        PlaylistTrack.createPlaylistTrack(playlistTrack);
    }

    /**
     * Remove a track from the given position.
     *
     * @param playlistId Playlist ID
     * @param number Position to remove
     * @return Removed track ID, or null if no track could be found ot the specified number
     */
    public String removePlaylistTrack(String playlistId, Integer number) {
        // Get track at the specified number
        final Handle handle = ThreadLocalContext.get().getHandle();
        String trackId = handle.createQuery("select pt.track_id from t_playlist_track pt where pt.playlist_id = :playlistId and pt.number = :number")
                .bind("playlistId", playlistId)
                .bind("number", number)
                .map(StringMapper.FIRST)
                .first();
        if (trackId == null) {
            return null;
        }

        // Delete the track
        handle.createStatement("delete from t_playlist_track pt where pt.playlist_id = :playlistId and pt.number = :number")
                .bind("playlistId", playlistId)
                .bind("number", number)
                .execute();

        // Reorder currrent tracks
        handle.createStatement("update t_playlist_track pt set pt.number = pt.number - 1 where pt.playlist_id = :playlistId and pt.number > :number")
                .bind("playlistId", playlistId)
                .bind("number", number)
                .execute();

        return trackId;
    }
}
