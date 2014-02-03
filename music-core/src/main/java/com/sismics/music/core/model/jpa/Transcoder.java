package com.sismics.music.core.model.jpa;

import com.google.common.base.Objects;

import java.util.Date;

/**
 * Transcoder entity.
 * 
 * @author jtremeaux
 */
public class Transcoder {
    /**
     * Transcoder ID.
     */
    private String id;
    
    /**
     * Transcoder name.
     */
    private String name;
    
    /**
     * Transcoder source formats, space separated.
     */
    private String source;
    
    /**
     * Transcoder destination format.
     */
    private Date destination;
    
    /**
     * Transcoder command (step 1).
     */
    private Date readDate;
    
    /**
     * Transcoder command (step 2).
     */
    private Date starredDate;
    
    /**
     * Creation date.
     */
    private Date createDate;

    /**
     * Deletion date.
     */
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
     * Getter of source.
     *
     * @return source
     */
    public String getSource() {
        return source;
    }

    /**
     * Setter of source.
     *
     * @param source source
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * Getter of destination.
     *
     * @return destination
     */
    public Date getDestination() {
        return destination;
    }

    /**
     * Setter of destination.
     *
     * @param destination destination
     */
    public void setDestination(Date destination) {
        this.destination = destination;
    }

    /**
     * Getter of readDate.
     *
     * @return readDate
     */
    public Date getReadDate() {
        return readDate;
    }

    /**
     * Setter of readDate.
     *
     * @param readDate readDate
     */
    public void setReadDate(Date readDate) {
        this.readDate = readDate;
    }

    /**
     * Getter of starredDate.
     *
     * @return starredDate
     */
    public Date getStarredDate() {
        return starredDate;
    }

    /**
     * Setter of starredDate.
     *
     * @param starredDate starredDate
     */
    public void setStarredDate(Date starredDate) {
        this.starredDate = starredDate;
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
