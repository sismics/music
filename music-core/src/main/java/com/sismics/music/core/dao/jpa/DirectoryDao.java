package com.sismics.music.core.dao.jpa;

import com.sismics.music.core.model.jpa.Directory;
import com.sismics.util.context.ThreadLocalContext;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.Query;

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
        if (directory.getName() == null) {
            directory.updateNameFromLocation();
        }

//        // Checks for directory unicity
//        Query q = em.createQuery("select u from Directory u where d.directoryname = :directoryname and d.deleteDate is null");
//        q.setParameter("directoryname", directory.getDirectoryname());
//        List<?> l = q.getResultList();
//        if (l.size() > 0) {
//            throw new Exception("AlreadyExistingDirectoryname");
//        }

        Handle handle = ThreadLocalContext.get().getHandle();
        handle.createStatement("insert into " +
                " T_DIRECTORY(DIR_ID_C, DIR_NAME_C, DIR_LOCATION_C, DIR_CREATEDATE_D)" +
                " values(:id, :name, :location, :createDate)")
                .bind("id", directory.getId())
                .bind("name", directory.getName())
                .bind("location", directory.getLocation())
                .bind("createDate", directory.getCreateDate())
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
        if (directory.getName() == null) {
            directory.updateNameFromLocation();
        }

        Handle handle = ThreadLocalContext.get().getHandle();
        handle.createStatement("update T_DIRECTORY d set " +
                " d.DIR_NAME_C = :name," +
                " d.DIR_LOCATION_C = :location " +
                " d.DIR_DISABLEDATE_D = :disableDate " +
                " where d.DIR_ID_C = :id")
                .bind("id", directory.getId())
                .bind("name", directory.getName())
                .bind("location", directory.getLocation())
                .bind("disableDate", directory.getLocation())
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
        Handle handle = ThreadLocalContext.get().getHandle();
        Query q = handle.createQuery("select d.DIR_ID_C, d.DIR_NAME_C, d.DIR_LOCATION_C, d.DIR_DISABLEDATE_D, d.DIR_CREATEDATE_D, d.DIR_DELETEDATE_D" +
                "  from T_DIRECTORY d" +
                "  where d.DIR_ID_C = :id and d.DIR_DELETEDATE_D is null")
                .bind("id", id);
        return (Directory) q.first(Directory.class);
    }
    
    /**
     * Deletes a directory.
     * 
     * @param id Directory ID
     */
    public void delete(String id) {
        Handle handle = ThreadLocalContext.get().getHandle();
        handle.createStatement("update T_DIRECTORY d" +
                "  set d.DIR_DELETEDATE_D = :deleteDate" +
                "  where d.DIR_ID_C = :id and d.DIR_DELETEDATE_D is null")
                .bind("id", id)
                .bind("deleteDate", new Date())
                .execute();
            
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
    public List<Directory> findAllEnabled() {
        Handle handle = ThreadLocalContext.get().getHandle();
        Query q = handle.createQuery("select d.DIR_ID_C, d.DIR_NAME_C, d.DIR_LOCATION_C, d.DIR_DISABLEDATE_D, d.DIR_CREATEDATE_D, d.DIR_DELETEDATE_D " +
                "  from d.T_DIRECTORY " +
                "  where d.DIR_DELETEDATE_D and d.DIR_DISABLEDATE_D is null " +
                "order by d.DIR_NAME_C is null");
        return (List<Directory>) q.list(Directory.class);
    }

    /**
     * Returns the list of all directories.
     *
     * @return List of directories
     */
    public List<Directory> findAll() {
        Handle handle = ThreadLocalContext.get().getHandle();
        Query q = handle.createQuery("select d.DIR_ID_C, d.DIR_NAME_C, d.DIR_LOCATION_C, d.DIR_DISABLEDATE_D, d.DIR_CREATEDATE_D, d.DIR_DELETEDATE_D " +
                "  from d.T_DIRECTORY " +
                "  where d.DIR_DELETEDATE_D " +
                "  order by d.DIR_NAME_C is null");
        return (List<Directory>) q.list(Directory.class);
    }
}
