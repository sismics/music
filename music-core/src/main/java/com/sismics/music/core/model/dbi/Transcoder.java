package com.sismics.music.core.model.dbi;

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
    private String destination;
    
    /**
     * Transcoder command (step 1).
     */
    private String step1;
    
    /**
     * Transcoder command (step 2).
     */
    private String step2;
    
    /**
     * Creation date.
     */
    private Date createDate;

    /**
     * Deletion date.
     */
    private Date deleteDate;

    public Transcoder() {
    }

    public Transcoder(String id, String name, String source, String destination, String step1, String step2, Date createDate, Date deleteDate) {
        this.id = id;
        this.name = name;
        this.source = source;
        this.destination = destination;
        this.step1 = step1;
        this.step2 = step2;
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
    public String getDestination() {
        return destination;
    }

    /**
     * Setter of destination.
     *
     * @param destination destination
     */
    public void setDestination(String destination) {
        this.destination = destination;
    }

    /**
     * Getter of step1.
     *
     * @return step1
     */
    public String getStep1() {
        return step1;
    }

    /**
     * Setter of step1.
     *
     * @param step1 step1
     */
    public void setStep1(String step1) {
        this.step1 = step1;
    }

    /**
     * Getter of step2.
     *
     * @return step2
     */
    public String getStep2() {
        return step2;
    }

    /**
     * Setter of step2.
     *
     * @param step2 step2
     */
    public void setStep2(String step2) {
        this.step2 = step2;
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
