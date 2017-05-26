package com.sismics.music.core.model.dbi;

import com.google.common.base.Objects;
import com.sismics.music.core.dao.dbi.mapper.AlbumMapper;
import com.sismics.util.context.ThreadLocalContext;
import org.skife.jdbi.v2.Handle;

import java.util.Date;

/**
 * Album entity.
 * 
 * @author jtremeaux
 */
public class Album {
    /**
     * Album ID.
     */
    private String id;

    /**
     * Directory ID.
     */
    private String directoryId;

    /**
     * Artist ID.
     */
    private String artistId;

    /**
     * Album title.
     */
    private String name;

    /**
     * Album art ID (same as the file name).
     */
    private String albumArt;

    /**
     * Creation date.
     */
    private Date createDate;
    
    /**
     * Last update date.
     */
    private Date updateDate;
    
    /**
     * Deletion date.
     */
    private Date deleteDate;
    
    /**
     * Location.
     */
    private String location;

    public Album() {
    }

    public Album(String id) {
        this.id = id;
    }

    public Album(String id, String directoryId, String artistId, String name, String albumArt, Date createDate, Date updateDate, Date deleteDate, String location) {
        this.id = id;
        this.directoryId = directoryId;
        this.artistId = artistId;
        this.name = name;
        this.albumArt = albumArt;
        this.createDate = createDate;
        this.updateDate = updateDate;
        this.deleteDate = deleteDate;
        this.location = location;
    }

    /**
     * Gets an active album by its ID.
     *
     * @param id Album ID
     * @return Album
     */
    public static Album getActiveById(String id) {
        final Handle handle = ThreadLocalContext.get().getHandle();
        return handle.createQuery("select " + new AlbumMapper().getJoinedColumns("a") +
                "  from t_album a" +
                "  where a.id = :id and a.deletedate is null")
                .bind("id", id)
                .mapTo(Album.class)
                .first();
    }

    /**
     * Getter of id.
     *
     * @return id
     */
    public String getId() {
        return id;
    }

    /**
     * Setter of id.
     *
     * @param id id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Getter of directoryId.
     *
     * @return directoryId
     */
    public String getDirectoryId() {
        return directoryId;
    }

    /**
     * Setter of directoryId.
     *
     * @param directoryId directoryId
     */
    public void setDirectoryId(String directoryId) {
        this.directoryId = directoryId;
    }

    /**
     * Getter of artistId.
     *
     * @return artistId
     */
    public String getArtistId() {
        return artistId;
    }

    /**
     * Setter of artistId.
     *
     * @param artistId artistId
     */
    public void setArtistId(String artistId) {
        this.artistId = artistId;
    }

    /**
     * Getter of name.
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Setter of name.
     *
     * @param name name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter of albumArt.
     *
     * @return albumArt
     */
    public String getAlbumArt() {
        return albumArt;
    }

    /**
     * Setter of albumArt.
     *
     * @param albumArt albumArt
     */
    public void setAlbumArt(String albumArt) {
        this.albumArt = albumArt;
    }

    /**
     * Getter of createDate.
     *
     * @return createDate
     */
    public Date getCreateDate() {
        return createDate;
    }

    /**
     * Setter of createDate.
     *
     * @param createDate createDate
     */
    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    /**
     * Getter of updateDate.
     *
     * @return updateDate
     */
    public Date getUpdateDate() {
        return updateDate;
    }

    /**
     * Setter of updateDate.
     *
     * @param updateDate updateDate
     */
    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    /**
     * Getter of deleteDate.
     *
     * @return deleteDate
     */
    public Date getDeleteDate() {
        return deleteDate;
    }

    /**
     * Setter of deleteDate.
     *
     * @param deleteDate deleteDate
     */
    public void setDeleteDate(Date deleteDate) {
        this.deleteDate = deleteDate;
    }
    
    /**
     * Getter of location.
     *
     * @return the location
     */
    public String getLocation() {
        return location;
    }

    /**
     * Setter of location.
     *
     * @param location location
     */
    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", id)
                .add("name", name)
                .toString();
    }
}
