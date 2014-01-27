package com.sismics.music.core.dao.jpa;

import com.sismics.music.core.model.jpa.Playlist;
import com.sismics.music.core.model.jpa.PlaylistTrack;
import com.sismics.music.core.model.jpa.Track;
import com.sismics.music.core.model.jpa.User;
import com.sismics.util.context.ThreadLocalContext;
import org.mindrot.jbcrypt.BCrypt;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.UUID;

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
        // Create the playlist UUID
        playlistTrack.setId(UUID.randomUUID().toString());

        EntityManager em = ThreadLocalContext.get().getEntityManager();
        em.persist(playlistTrack);

        return playlistTrack.getId();
    }

    /**
     * Returns a playlist by playlist ID.
     *
     * @param playlistId Playlist ID
     */
    public void deleteByPlaylistId(String playlistId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createQuery("delete from PlaylistTrack pt where pt.playlistId = :playlistId");
        q.setParameter("playlistId", playlistId);
        q.executeUpdate();
    }

    /**
     * Returns the last order in the playlist, or 0 if the playlist is empty.
     *
     * @param playlistId Playlist ID
     */
    public Integer getPlaylistTrackLastOrder(String playlistId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createNativeQuery("select max(pt.PLT_ORDER_N) from T_PLAYLIST_TRACK pt where pt.PLT_IDPLAYLIST_C = :playlistId");
        q.setParameter("playlistId", playlistId);
        Integer order = (Integer) q.getSingleResult();
        if (order == null) {
            order = 0;
        }
        return order;
    }

    /**
     * Insert a track at the given position.
     *
     * @param playlistId Playlist ID
     * @param trackId ID of the track to insert
     * @param order Position to insert
     */
    public void insertPlaylistTrack(String playlistId, String trackId, Integer order) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();

        // Reorder currrent tracks
        Query q = em.createNativeQuery("update T_PLAYLIST_TRACK pt set pt.PLT_ORDER_N = pt.PLT_ORDER_N + 1 where pt.PLT_IDPLAYLIST_C = :playlistId and pt.PLT_ORDER_N >= :order");
        q.setParameter("playlistId", playlistId);
        q.setParameter("order", order);
        q.executeUpdate();

        // Insert new track
        PlaylistTrack playlistTrack = new PlaylistTrack();
        playlistTrack.setPlaylistId(playlistId);
        playlistTrack.setTrackId(trackId);
        playlistTrack.setOrder(order);
        create(playlistTrack);
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
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createNativeQuery("select pt.TRK_ID_C from T_PLAYLIST_TRACK pt where pt.PLT_IDPLAYLIST_C = :playlistId and pt.PLT_ORDER_C = :order");
        q.setParameter("playlistId", playlistId);
        q.setParameter("order", order);
        String trackId = null;
        try {
            trackId = (String) q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }

        // Delete the track
        q = em.createNativeQuery("delete from T_PLAYLIST_TRACK pt where pt.PLT_IDPLAYLIST_C = :playlistId and pt.PLT_ORDER_C = :order");
        q.setParameter("playlistId", playlistId);
        q.setParameter("order", order);
        q.executeUpdate();

        // Reorder currrent tracks
        q = em.createNativeQuery("update T_PLAYLIST_TRACK pt set pt.PLT_ORDER_N = pt.PLT_ORDER_N - 1 where pt.PLT_IDPLAYLIST_C = :playlistId and pt.PLT_ORDER_N > :order");
        q.setParameter("playlistId", playlistId);
        q.setParameter("order", order);
        q.executeUpdate();

        return trackId;
    }
}
