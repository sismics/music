package com.sismics.music.core.dao.jpa;

import com.sismics.music.core.model.jpa.Artist;
import com.sismics.util.context.ThreadLocalContext;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.Date;
import java.util.UUID;

/**
 * Artist DAO.
 * 
 * @author jtremeaux
 */
public class ArtistDao {
    /**
     * Creates a new artist.
     * 
     * @param artist Artist to create
     * @return Artist ID
     */
    public String create(Artist artist) {
        artist.setId(UUID.randomUUID().toString());
        artist.setCreateDate(new Date());

        EntityManager em = ThreadLocalContext.get().getEntityManager();
        em.persist(artist);
        
        return artist.getId();
    }
    
    /**
     * Updates a artist.
     * 
     * @param artist Artist to update
     * @return Updated artist
     */
    public Artist update(Artist artist) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();

        // Get the artist
        Query q = em.createQuery("select a from Artist a where a.id = :id and a.deleteDate is null");
        q.setParameter("id", artist.getId());
        Artist artistFromDb = (Artist) q.getSingleResult();

        // Update the artist

        return artist;
    }
    
    /**
     * Gets an active artist by its name.
     * 
     * @param name Artist name
     * @return Artist
     */
    public Artist getActiveByName(String name) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        try {
            Query q = em.createQuery("select a from Artist a where a.name = :name and a.deleteDate is null");
            q.setParameter("name", name);
            return (Artist) q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    /**
     * Gets an active artist by its artistname.
     *
     * @param id Artist ID
     * @return Artist
     */
    public Artist getActiveById(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        try {
            Query q = em.createQuery("select a from Artist a where a.id = :id and a.deleteDate is null");
            q.setParameter("id", id);
            return (Artist) q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Deletes a artist.
     * 
     * @param id Artist ID
     */
    public void delete(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
            
        // Get the artist
        Query q = em.createQuery("select a from Artist a where a.id = :id and a.deleteDate is null");
        q.setParameter("id", id);
        Artist artistFromDb = (Artist) q.getSingleResult();
        
        // Delete the artist
        Date dateNow = new Date();
        artistFromDb.setDeleteDate(dateNow);
    }
}
