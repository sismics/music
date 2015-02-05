package com.sismics.music.core.model.dbi;

import com.google.common.base.Objects;

import java.util.Date;

/**
 * Artist entity.
 * 
 * @author jtremeaux
 */
public class Artist {
    /**
     * Album ID.
     */
    private String id;

    /**
     * Artist name.
     */
    private String name;

    /**
     * Artist name corrected.
     */
    private String nameCorrected;
    
    /**
     * Creation date.
     */
    private Date createDate;
    
    /**
     * Deletion date.
     */
    private Date deleteDate;

    public Artist() {
    }

    public Artist(String id, String name, String nameCorrected, Date createDate, Date deleteDate) {
        this.id = id;
        this.name = name;
        this.nameCorrected = nameCorrected;
        this.createDate = createDate;
        this.deleteDate = deleteDate;
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
     * Getter of nameCorrected.
     *
     * @return the nameCorrected
     */
    public String getNameCorrected() {
        return nameCorrected;
    }

    /**
     * Setter of nameCorrected.
     *
     * @param nameCorrected nameCorrected
     */
    public void setNameCorrected(String nameCorrected) {
        this.nameCorrected = nameCorrected;
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
