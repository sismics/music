package com.sismics.music.core.model.dbi;

import java.util.Date;

import com.google.common.base.Objects;

/**
 * Directory entity.
 * 
 * @author jtremeaux
 */
public class Directory {
    /**
     * Album ID.
     */
    private String id;

    /**
     * Directory location.
     */
    private String location;

    /**
     * Disable date.
     */
    private Date disableDate;
    
    /**
     * Creation date.
     */
    private Date createDate;

    /**
     * Deletion date.
     */
    private Date deleteDate;

    public Directory() {
    }

    public Directory(String id, String location, Date disableDate, Date createDate, Date deleteDate) {
        this.id = id;
        this.location = location;
        this.disableDate = disableDate;
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
     * Getter of location.
     *
     * @return location
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
     * Getter of disableDate.
     *
     * @return disableDate
     */
    public Date getDisableDate() {
        return disableDate;
    }

    /**
     * Setter of disableDate.
     *
     * @param disableDate disableDate
     */
    public void setDisableDate(Date disableDate) {
        this.disableDate = disableDate;
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
     * Normalize the directory location.
     */
    public void normalizeLocation() {
        location.replaceAll("\\\\", "/");
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Directory) {
            return id.equals(((Directory) obj).id);
        }
        return false;
    }
    
    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", id)
                .add("location", location)
                .toString();
    }
}
