package com.sismics.music.core.model.jpa;

import com.google.common.base.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * Album entity.
 * 
 * @author jtremeaux
 */
@Entity
@Table(name = "T_ALBUM")
public class Album {
    /**
     * Album ID.
     */
    @Id
    @Column(name = "ALB_ID_C", length = 36)
    private String id;

    /**
     * Directory ID.
     */
    @Column(name = "ALB_IDDIRECTORY_C", nullable = false, length = 36)
    private String directoryId;

    /**
     * Artist ID.
     */
    @Column(name = "ALB_IDARTIST_C", nullable = false, length = 36)
    private String artistId;

    /**
     * Album title.
     */
    @Column(name = "ALB_NAME_C", nullable = false, length = 100)
    private String name;

    /**
     * Creation date.
     */
    @Column(name = "ALB_CREATEDATE_D", nullable = false)
    private Date createDate;
    
    /**
     * Deletion date.
     */
    @Column(name = "ALB_DELETEDATE_D")
    private Date deleteDate;
    
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

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", id)
                .add("name", name)
                .toString();
    }
}
