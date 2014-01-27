package com.sismics.music.core.dao.jpa;

import com.sismics.music.core.model.jpa.Directory;
import com.sismics.music.core.model.jpa.Playlist;
import com.sismics.util.context.ThreadLocalContext;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.Date;
import java.util.List;
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
        // Create the playlist UUID
        playlist.setId(UUID.randomUUID().toString());

        EntityManager em = ThreadLocalContext.get().getEntityManager();
        em.persist(playlist);
        
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
