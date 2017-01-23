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
                "  T_PLAYLIST_TRACK (PLT_ID_C, PLT_IDPLAYLIST_C, PLT_IDTRACK_C, PLT_ORDER_N)" +
                "  values(:id, :playlistId, :trackId, :order)")
                .bind("id", playlistTrack.getId())
                .bind("playlistId", playlistTrack.getPlaylistId())
                .bind("trackId", playlistTrack.getTrackId())
                .bind("order", playlistTrack.getOrder())
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
        handle.createStatement("delete from T_PLAYLIST_TRACK pt" +
                "  where pt.PLT_IDPLAYLIST_C = :playlistId ")
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
        Object order = handle.createQuery("select max(pt.PLT_ORDER_N) from T_PLAYLIST_TRACK pt where pt.PLT_IDPLAYLIST_C = :playlistId")
                .bind("playlistId", playlistId)
                .map(ObjectMapper.FIRST)
                .first();
        return order == null ? 0 : ((Integer) order) + 1;
    }

    /**
     * Insert a track at the given position.
     *
     * @param playlistId Playlist ID
     * @param trackId ID of the track to insert
     * @param order Position to insert
     */
    public void insertPlaylistTrack(String playlistId, String trackId, Integer order) {
        // Reorder currrent tracks
        final Handle handle = ThreadLocalContext.get().getHandle();
        handle.createStatement("update T_PLAYLIST_TRACK pt set pt.PLT_ORDER_N = pt.PLT_ORDER_N + 1 where pt.PLT_IDPLAYLIST_C = :playlistId and pt.PLT_ORDER_N >= :order")
                .bind("playlistId", playlistId)
                .bind("order", order)
                .execute();

        // Insert new track
        PlaylistTrack playlistTrack = new PlaylistTrack();
        playlistTrack.setPlaylistId(playlistId);
        playlistTrack.setTrackId(trackId);
        playlistTrack.setOrder(order);
        PlaylistTrack.createPlaylistTrack(playlistTrack);
    }

    /**
     * Remove a track from the given position.
     *
     * @param playlistId Playlist ID
     * @param order Position to remove
     * @return Removed track ID, or null if no track could be found ot the specified order
     */
    public String removePlaylistTrack(String playlistId, Integer order) {
        // Get track at the specified order
        final Handle handle = ThreadLocalContext.get().getHandle();
        String trackId = handle.createQuery("select pt.PLT_IDTRACK_C from T_PLAYLIST_TRACK pt where pt.PLT_IDPLAYLIST_C = :playlistId and pt.PLT_ORDER_N = :order")
                .bind("playlistId", playlistId)
                .bind("order", order)
                .map(StringMapper.FIRST)
                .first();
        if (trackId == null) {
            return null;
        }

        // Delete the track
        handle.createStatement("delete from T_PLAYLIST_TRACK pt where pt.PLT_IDPLAYLIST_C = :playlistId and pt.PLT_ORDER_N = :order")
                .bind("playlistId", playlistId)
                .bind("order", order)
                .execute();

        // Reorder currrent tracks
        handle.createStatement("update T_PLAYLIST_TRACK pt set pt.PLT_ORDER_N = pt.PLT_ORDER_N - 1 where pt.PLT_IDPLAYLIST_C = :playlistId and pt.PLT_ORDER_N > :order")
                .bind("playlistId", playlistId)
                .bind("order", order)
                .execute();

        return trackId;
    }
}
