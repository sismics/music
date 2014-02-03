package com.sismics.music.core.model.jpa;

import com.google.common.base.Objects;

import java.util.Date;

/**
 * Authentication token entity.
 * 
 * @author jtremeaux
 */
public class AuthenticationToken {
    /**
     * Token.
     */
    private String id;

    /**
     * User ID.
     */
    private String userId;
    
    /**
     * Remember the user next time (long lasted session).
     */
    private boolean longLasted;
    
    /**
     * Token creation date.
     */
    private Date creationDate;

    /**
     * Last connection date using this token.
     */
    private Date lastConnectionDate;

    public AuthenticationToken(String id, String userId, boolean longLasted, Date creationDate, Date lastConnectionDate) {
        this.id = id;
        this.userId = userId;
        this.longLasted = longLasted;
        this.creationDate = creationDate;
        this.lastConnectionDate = lastConnectionDate;
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
     * Getter of userId.
     *
     * @return userId
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Setter of userId.
     *
     * @param userId userId
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Getter of longLasted.
     *
     * @return longLasted
     */
    public boolean isLongLasted() {
        return longLasted;
    }

    /**
     * Setter of longLasted.
     *
     * @param longLasted longLasted
     */
    public void setLongLasted(boolean longLasted) {
        this.longLasted = longLasted;
    }

    /**
     * Getter of creationDate.
     *
     * @return creationDate
     */
    public Date getCreationDate() {
        return creationDate;
    }

    /**
     * Setter of creationDate.
     *
     * @param creationDate creationDate
     */
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * Getter of lastConnectionDate.
     *
     * @return lastConnectionDate
     */
    public Date getLastConnectionDate() {
        return lastConnectionDate;
    }

    /**
     * Setter of lastConnectionDate.
     *
     * @param lastConnectionDate lastConnectionDate
     */
    public void setLastConnectionDate(Date lastConnectionDate) {
        this.lastConnectionDate = lastConnectionDate;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", "**hidden**")
                .add("userId", userId)
                .add("longLasted", longLasted)
                .toString();
    }
}