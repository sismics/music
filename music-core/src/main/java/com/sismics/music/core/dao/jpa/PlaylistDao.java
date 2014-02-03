package com.sismics.music.core.dao.jpa;

import com.sismics.music.core.model.jpa.Playlist;
import com.sismics.util.context.ThreadLocalContext;
import org.skife.jdbi.v2.Handle;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.UUID;

/**
 * Playlist DAO.
 * 
 * @author jtremeaux
 */
public class PlaylistDao {
    /**
     * Creates a new playlist.
     * 
     * @param playlist Playlist to create
     * @return Playlist ID
     */
    public String create(Playlist playlist) {
        playlist.setId(UUID.randomUUID().toString());

        final Handle handle = ThreadLocalContext.get().getHandle();
        handle.createStatement("insert into " +
                "  T_PLAYLIST(PLL_ID_C, PLL_IDUSER_C)" +
                "  values(:id, :userId)")
                .bind("id", playlist.getId())
                .bind("userId", playlist.getUserId())
                .execute();

        return playlist.getId();
    }

    /**
     * Gets a playlist by user ID.
     *
     * @param userId User ID
     * @return Playlist
     */
    public Playlist getActiveByUserId(String userId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        try {
            Query q = em.createQuery("select p from Playlist p where p.userId = :userId");
            q.setParameter("userId", userId);
            return (Playlist) q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}
