package com.sismics.music.core.dao.dbi;

import com.sismics.music.core.model.dbi.Artist;
import com.sismics.util.context.ThreadLocalContext;
import org.skife.jdbi.v2.Handle;

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

        final Handle handle = ThreadLocalContext.get().getHandle();
        handle.createStatement("insert into " +
                "  T_ARTIST (ART_ID_C, ART_NAME_C, ART_CREATEDATE_D)" +
                "  values(:id, :name, :createDate)")
                .bind("id", artist.getId())
                .bind("name", artist.getName())
                .bind("createDate", artist.getCreateDate())
                .execute();

        return artist.getId();
    }
    
    /**
     * Updates a artist.
     * 
     * @param artist Artist to update
     * @return Updated artist
     */
    public Artist update(Artist artist) {

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
        final Handle handle = ThreadLocalContext.get().getHandle();
        return handle.createQuery("select a.ART_ID_C, a.ART_NAME_C, a.ART_CREATEDATE_D, a.ART_DELETEDATE_D" +
                "  from T_ARTIST a" +
                "  where lower(a.ART_NAME_C) = lower(:name) and a.ART_DELETEDATE_D is null")
                .bind("name", name)
                .mapTo(Artist.class)
                .first();
    }
    
    /**
     * Gets an active artist by its artistname.
     *
     * @param id Artist ID
     * @return Artist
     */
    public Artist getActiveById(String id) {
        final Handle handle = ThreadLocalContext.get().getHandle();
        return handle.createQuery("select a.ART_ID_C, a.ART_NAME_C, a.ART_CREATEDATE_D, a.ART_DELETEDATE_D" +
                "  from T_ARTIST a" +
                "  where a.ART_ID_C = :id and a.ART_DELETEDATE_D is null")
                .bind("id", id)
                .mapTo(Artist.class)
                .first();
    }

    /**
     * Deletes a artist.
     * 
     * @param id Artist ID
     */
    public void delete(String id) {
        final Handle handle = ThreadLocalContext.get().getHandle();
        handle.createStatement("update T_ARTIST a" +
                "  set a.ART_DELETEDATE_D = :deleteDate" +
                "  where a.ART_ID_C = :id and a.ART_DELETEDATE_D is null")
                .bind("id", id)
                .bind("deleteDate", new Date())
                .execute();
    }

    /**
     * Delete any artist that don't have any album.
     *
     * @param directoryId Directory ID
     */
    public void deleteEmptyArtist(String directoryId) {
        final Handle handle = ThreadLocalContext.get().getHandle();
        handle.createStatement("update T_ARTIST a set a.ART_DELETEDATE_D = :deleteDate where a.ART_ID_C NOT IN (" +
                "  select al.ALB_IDARTIST_C from T_ARTIST ea " +
                "    join T_ALBUM al on(ea.ART_ID_C = al.ALB_IDARTIST_C)" +
                "    where ea.ART_DELETEDATE_D is null and al.ALB_DELETEDATE_D is null and al.ALB_IDDIRECTORY_C = :directoryId" +
                "    group by al.ALB_IDARTIST_C" +
                ")")
                .bind("deleteDate", new Date())
                .bind("directoryId", directoryId)
                .execute();
    }
}
