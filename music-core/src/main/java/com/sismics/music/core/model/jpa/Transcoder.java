package com.sismics.music.core.model.jpa;

import com.google.common.base.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * Transcoder entity.
 * 
 * @author jtremeaux
 */
@Entity
@Table(name = "T_TRANSCODER")
public class Transcoder {
    /**
     * Transcoder ID.
     */
    @Id
    @Column(name = "TRN_ID_C", length = 36)
    private String id;
    
    /**
     * Transcoder name.
     */
    @Column(name = "TRN_NAME_C", nullable = false, length = 100)
    private String name;
    
    /**
     * Transcoder source formats, space separated.
     */
    @Column(name = "TRN_SOURCE_C", nullable = false, length = 1000)
    private String source;
    
    /**
     * Transcoder destination format.
     */
    @Column(name = "TRN_DESTINATION_C", nullable = false)
    private Date destination;
    
    /**
     * Transcoder command (step 1).
     */
    @Column(name = "TRN_READDATE_D")
    private Date readDate;
    
    /**
     * Transcoder command (step 2).
     */
    @Column(name = "TRN_STARREDDATE_D")
    private Date starredDate;
    
    /**
     * Creation date.
     */
    @Column(name = "TRN_CREATEDATE_D")
    private Date createDate;

    /**
     * Deletion date.
     */
    @Column(name = "TRN_DELETEDATE_D")
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
