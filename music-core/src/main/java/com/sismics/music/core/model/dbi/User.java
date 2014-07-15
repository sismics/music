package com.sismics.music.core.model.dbi;

import java.util.Date;

import com.google.common.base.Objects;

/**
 * User entity.
 * 
 * @author jtremeaux
 */
public class User {
    /**
     * User ID.
     */
    private String id;
    
    /**
     * Locale ID.
     */
    private String localeId;
    
    /**
     * Locale ID.
     */
    private String roleId;
    
    /**
     * User's username.
     */
    private String username;
    
    /**
     * User's password.
     */
    private String password;

    /**
     * Email address.
     */
    private String email;
    
    /**
     * Maximum bitrate in kbps (null if unlimited).
     */
    private Integer maxBitrate;

    /**
     * Session token on Last.fm.
     */
    private String lastFmSessionToken;

    /**
     * Scrobbling on Last.fm active.
     */
    private boolean lastFmActive;

    /**
     * True if the user hasn't dismissed the first connection screen.
     */
    private boolean firstConnection;

    /**
     * Creation date.
     */
    private Date createDate;
    
    /**
     * Deletion date.
     */
    private Date deleteDate;

    public User() {
    }

    public User(String id, String localeId, String roleId, String username, String password, String email, Integer maxBitrate, String lastFmSessionToken, boolean lastFmActive, boolean firstConnection, Date createDate, Date deleteDate) {
        this.id = id;
        this.localeId = localeId;
        this.roleId = roleId;
        this.username = username;
        this.password = password;
        this.email = email;
        this.maxBitrate = maxBitrate;
        this.lastFmSessionToken = lastFmSessionToken;
        this.lastFmActive = lastFmActive;
        this.firstConnection = firstConnection;
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
     * Getter of localeId.
     *
     * @return localeId
     */
    public String getLocaleId() {
        return localeId;
    }

    /**
     * Setter of localeId.
     *
     * @param localeId localeId
     */
    public void setLocaleId(String localeId) {
        this.localeId = localeId;
    }

    /**
     * Getter of roleId.
     *
     * @return roleId
     */
    public String getRoleId() {
        return roleId;
    }

    /**
     * Setter of roleId.
     *
     * @param roleId roleId
     */
    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    /**
     * Getter of username.
     *
     * @return username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Setter of username.
     *
     * @param username username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Getter of password.
     *
     * @return password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Setter of password.
     *
     * @param password password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Getter of email.
     *
     * @return email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Setter of email.
     *
     * @param email email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Getter of maxBitrate.
     *
     * @return maxBitrate
     */
    public Integer getMaxBitrate() {
        return maxBitrate;
    }

    /**
     * Setter of maxBitrate.
     *
     * @param maxBitrate maxBitrate
     */
    public void setMaxBitrate(Integer maxBitrate) {
        this.maxBitrate = maxBitrate;
    }

    /**
     * Getter of lastFmSessionToken.
     *
     * @return lastFmSessionToken
     */
    public String getLastFmSessionToken() {
        return lastFmSessionToken;
    }

    /**
     * Setter of lastFmSessionToken.
     *
     * @param lastFmSessionToken lastFmSessionToken
     */
    public void setLastFmSessionToken(String lastFmSessionToken) {
        this.lastFmSessionToken = lastFmSessionToken;
    }

    /**
     * Getter of lastFmActive.
     *
     * @return lastFmActive
     */
    public boolean isLastFmActive() {
        return lastFmActive;
    }

    /**
     * Setter of lastFmActive.
     *
     * @param lastFmActive lastFmActive
     */
    public void setLastFmActive(boolean lastFmActive) {
        this.lastFmActive = lastFmActive;
    }

    /**
     * Getter of firstConnection.
     *
     * @return firstConnection
     */
    public boolean isFirstConnection() {
        return firstConnection;
    }

    /**
     * Setter of firstConnection.
     *
     * @param firstConnection firstConnection
     */
    public void setFirstConnection(boolean firstConnection) {
        this.firstConnection = firstConnection;
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
                .add("username", username)
                .toString();
    }
}
