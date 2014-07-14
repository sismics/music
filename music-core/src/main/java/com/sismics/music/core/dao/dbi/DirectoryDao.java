package com.sismics.music.core.dao.dbi;

import com.sismics.music.core.model.dbi.Directory;
import com.sismics.util.context.ThreadLocalContext;
import org.skife.jdbi.v2.Handle;

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

        final Handle handle = ThreadLocalContext.get().getHandle();
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

        final Handle handle = ThreadLocalContext.get().getHandle();
        handle.createStatement("update T_DIRECTORY set " +
                " DIR_NAME_C = :name," +
                " DIR_LOCATION_C = :location, " +
                " DIR_DISABLEDATE_D = :disableDate " +
                " where DIR_ID_C = :id")
                .bind("id", directory.getId())
                .bind("name", directory.getName())
                .bind("location", directory.getLocation())
                .bind("disableDate", directory.getDisableDate())
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
        return handle.createQuery("select d.DIR_ID_C, d.DIR_NAME_C, d.DIR_LOCATION_C, d.DIR_DISABLEDATE_D, d.DIR_CREATEDATE_D, d.DIR_DELETEDATE_D" +
                "  from T_DIRECTORY d" +
                "  where d.DIR_ID_C = :id and d.DIR_DELETEDATE_D is null")
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
        handle.createStatement("update T_DIRECTORY d" +
                "  set d.DIR_DELETEDATE_D = :deleteDate" +
                "  where d.DIR_ID_C = :id and d.DIR_DELETEDATE_D is null")
                .bind("id", id)
                .bind("deleteDate", new Date())
                .execute();
    }

    /**
     * Returns the list of all enabled directories.
     *
     * @return List of directories
     */
    public List<Directory> findAllEnabled() {
        final Handle handle = ThreadLocalContext.get().getHandle();
        return handle.createQuery("select d.DIR_ID_C, d.DIR_NAME_C, d.DIR_LOCATION_C, d.DIR_DISABLEDATE_D, d.DIR_CREATEDATE_D, d.DIR_DELETEDATE_D " +
                "  from T_DIRECTORY d" +
                "  where d.DIR_DELETEDATE_D is null and d.DIR_DISABLEDATE_D is null " +
                "  order by d.DIR_NAME_C")
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
        return handle.createQuery("select d.DIR_ID_C, d.DIR_NAME_C, d.DIR_LOCATION_C, d.DIR_DISABLEDATE_D, d.DIR_CREATEDATE_D, d.DIR_DELETEDATE_D " +
                "  from T_DIRECTORY d" +
                "  where d.DIR_DELETEDATE_D is null" +
                "  order by d.DIR_NAME_C is null")
                .mapTo(Directory.class)
                .list();
    }
}
