package com.sismics.music.core.dao.dbi;

import com.sismics.music.core.model.dbi.Directory;
import com.sismics.util.context.ThreadLocalContext;
import org.skife.jdbi.v2.Handle;

import java.sql.Timestamp;
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
        directory.setId(UUID.randomUUID().toString());
        directory.setCreateDate(new Date());
        directory.normalizeLocation();

//        // Checks for directory unicity
//        Query q = em.createQuery("select u from Directory u where d.directoryname = :directoryname and d.deleteDate is null");
//        q.setParameter("directoryname", directory.getDirectoryname());
//        List<?> l = q.getResultList();
//        if (l.size() > 0) {
//            throw new Exception("AlreadyExistingDirectoryname");
//        }

        final Handle handle = ThreadLocalContext.get().getHandle();
        handle.createStatement("insert into " +
                " t_directory(id, location, createdate)" +
                " values(:id, :location, :createDate)")
                .bind("id", directory.getId())
                .bind("location", directory.getLocation())
                .bind("createDate", new Timestamp(directory.getCreateDate().getTime()))
                .execute();

        return directory.getId();
    }
    
    /**
     * Updates a directory.
     * 
     * @param directory Directory to update
     * @return Updated directory
     */
    public Directory update(Directory directory) {
        directory.normalizeLocation();

        final Handle handle = ThreadLocalContext.get().getHandle();
        handle.createStatement("update t_directory set " +
                " location = :location, " +
                " disabledate = :disableDate " +
                " where id = :id")
                .bind("id", directory.getId())
                .bind("location", directory.getLocation())
                .bind("disableDate", directory.getDisableDate() == null ? null : new Timestamp(directory.getDisableDate().getTime()))
                .execute();

        return directory;
    }
    
    /**
     * Gets an active directory by its ID.
     * 
     * @param id Directory ID
     * @return Directory
     */
    public Directory getActiveById(String id) {
        final Handle handle = ThreadLocalContext.get().getHandle();
        return handle.createQuery("select d.id, d.location, d.disabledate, d.createdate, d.deletedate" +
                "  from t_directory d" +
                "  where d.id = :id and d.deletedate is null")
                .bind("id", id)
                .mapTo(Directory.class)
                .first();
    }
    
    /**
     * Deletes a directory.
     * 
     * @param id Directory ID
     */
    public void delete(String id) {
        final Handle handle = ThreadLocalContext.get().getHandle();
        handle.createStatement("update t_directory d" +
                "  set d.deletedate = :deleteDate" +
                "  where d.id = :id and d.deletedate is null")
                .bind("id", id)
                .bind("deleteDate", new Timestamp(new Date().getTime()))
                .execute();
    }

    /**
     * Returns the list of all enabled directories.
     *
     * @return List of directories
     */
    public List<Directory> findAllEnabled() {
        final Handle handle = ThreadLocalContext.get().getHandle();
        return handle.createQuery("select d.id, d.location, d.disabledate, d.createdate, d.deletedate " +
                "  from t_directory d" +
                "  where d.deletedate is null and d.disabledate is null " +
                "  order by d.location")
                .mapTo(Directory.class)
                .list();
    }

    /**
     * Returns the list of all directories.
     *
     * @return List of directories
     */
    public List<Directory> findAll() {
        final Handle handle = ThreadLocalContext.get().getHandle();
        return handle.createQuery("select d.id, d.location, d.disabledate, d.createdate, d.deletedate " +
                "  from t_directory d" +
                "  where d.deletedate is null" +
                "  order by d.location")
                .mapTo(Directory.class)
                .list();
    }
}
