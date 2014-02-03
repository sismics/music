package com.sismics.music.core.dao.jpa;

import com.sismics.music.core.model.jpa.Directory;
import com.sismics.util.context.ThreadLocalContext;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Directory DAO.
 * 
 * @author jtremeaux
 */
public class DirectoryDao {
    /**
     * Creates a new directory.
     * 
     * @param directory Directory to create
     * @return Directory ID
     */
    public String create(Directory directory) {
        // Create the directory UUID
        directory.setId(UUID.randomUUID().toString());

        directory.normalizeLocation();
        if (directory.getName() == null) {
            directory.updateNameFromLocation();
        }

//        // Checks for directory unicity
        EntityManager em = ThreadLocalContext.get().getEntityManager();
//        Query q = em.createQuery("select u from Directory u where d.directoryname = :directoryname and d.deleteDate is null");
//        q.setParameter("directoryname", directory.getDirectoryname());
//        List<?> l = q.getResultList();
//        if (l.size() > 0) {
//            throw new Exception("AlreadyExistingDirectoryname");
//        }
        
        directory.setCreateDate(new Date());
        em.persist(directory);
        
        return directory.getId();
    }
    
    /**
     * Updates a directory.
     * 
     * @param directory Directory to update
     * @return Updated directory
     */
    public Directory update(Directory directory) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();

        directory.normalizeLocation();
        if (directory.getName() == null) {
            directory.updateNameFromLocation();
        }

        // Get the directory
        Query q = em.createQuery("select d from Directory d where d.id = :id and d.deleteDate is null");
        q.setParameter("id", directory.getId());
        Directory directoryFromDb = (Directory) q.getSingleResult();

        // Update the directory
        directoryFromDb.setName(directory.getName());
        directoryFromDb.setLocation(directory.getLocation());
        if (directoryFromDb.getDisableDate() == null && directory.getDisableDate() != null) {
            directoryFromDb.setDisableDate(directory.getDisableDate());

            // TODO remove this dir from the index
        } else if (directoryFromDb.getDisableDate() != null && directory.getDisableDate() == null) {
            directoryFromDb.setDisableDate(null);

            // TODO add this dir to the index
        }

        return directory;
    }
    
    /**
     * Gets an active directory by its ID.
     * 
     * @param id Directory ID
     * @return Directory
     */
    public Directory getActiveById(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        try {
            Query q = em.createQuery("select d from Directory d where d.id = :id and d.deleteDate is null");
            q.setParameter("id", id);
            return (Directory) q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    /**
     * Deletes a directory.
     * 
     * @param id Directory ID
     */
    public void delete(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
            
        // Get the directory
        Query q = em.createQuery("select d from Directory d where d.id = :id and d.deleteDate is null");
        q.setParameter("id", id);
        Directory directoryFromDb = (Directory) q.getSingleResult();
        
        // Delete the directory
        Date dateNow = new Date();
        directoryFromDb.setDeleteDate(dateNow);

        // TODO Delete linked data (albums + tracks + artists?)
//        q = em.createQuery("delete from AuthenticationToken at where at.directoryId = :directoryId");
//        q.setParameter("directoryId", directoryFromDb.getId());
//        q.executeUpdate();
    }

    /**
     * Returns the list of all enabled directories.
     *
     * @return List of directories
     */
    @SuppressWarnings("unchecked")
    public List<Directory> findAllEnabled() {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createQuery("select d from Directory d where d.deleteDate is null and d.disableDate is null order by d.name");
        return q.getResultList();
    }

    /**
     * Returns the list of all directories.
     *
     * @return List of directories
     */
    @SuppressWarnings("unchecked")
    public List<Directory> findAll() {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createQuery("select d from Directory d where d.deleteDate is null order by d.name");
        return q.getResultList();
    }
}
