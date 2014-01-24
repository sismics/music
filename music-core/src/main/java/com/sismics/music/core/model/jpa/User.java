package com.sismics.music.core.model.jpa;

import com.google.common.base.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * User entity.
 * 
 * @author jtremeaux
 */
@Entity
@Table(name = "T_USER")
public class User {
    /**
     * User ID.
     */
    @Id
    @Column(name = "USE_ID_C", length = 36)
    private String id;
    
    /**
     * Locale ID.
     */
    @Column(name = "USE_IDLOCALE_C", nullable = false, length = 10)
    private String localeId;
    
    /**
     * Locale ID.
     */
    @Column(name = "USE_IDROLE_C", nullable = false, length = 36)
    private String roleId;
    
    /**
     * User's username.
     */
    @Column(name = "USE_USERNAME_C", nullable = false, length = 50)
    private String username;
    
    /**
     * User's password.
     */
    @Column(name = "USE_PASSWORD_C", nullable = false, length = 100)
    private String password;

    /**
     * Email address.
     */
    @Column(name = "USE_EMAIL_C", nullable = false, length = 100)
    private String email;
    
    /**
     * Theme.
     */
    @Column(name = "USE_THEME_C", nullable = false, length = 100)
    private String theme;
    
    /**
     * Maximum bitrate in kbps (null if unlimited).
     */
    @Column(name = "USE_MAXBITRATE_N")
    private Integer maxBitrate;

    /**
     * User login on Last.fm.
     */
    @Column(name = "USE_LASTFMLOGIN_C")
    private String lastFmLogin;

    /**
     * User password on Last.fm.
     */
    @Column(name = "USE_LASTFMPASSWORD_C")
    private String lastFmPassword;

    /**
     * Scrobbling on Last.fm active.
     */
    @Column(name = "USE_LASTFMACTIVE_B")
    private boolean lastFmActive;

    /**
     * True if the user hasn't dismissed the first connection screen.
     */
    @Column(name = "USE_FIRSTCONNECTION_B", nullable = false)
    private boolean firstConnection;

    /**
     * Creation date.
     */
    @Column(name = "USE_CREATEDATE_D", nullable = false)
    private Date createDate;
    
    /**
     * Deletion date.
     */
    @Column(name = "USE_DELETEDATE_D")
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
     * Getter of theme.
     *
     * @return theme
     */
    public String getTheme() {
        return theme;
    }

    /**
     * Setter of theme.
     *
     * @param theme theme
     */
    public void setTheme(String theme) {
        this.theme = theme;
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
     * Getter of lastFmLogin.
     *
     * @return lastFmLogin
     */
    public String getLastFmLogin() {
        return lastFmLogin;
    }

    /**
     * Setter of lastFmLogin.
     *
     * @param lastFmLogin lastFmLogin
     */
    public void setLastFmLogin(String lastFmLogin) {
        this.lastFmLogin = lastFmLogin;
    }

    /**
     * Getter of lastFmPassword.
     *
     * @return lastFmPassword
     */
    public String getLastFmPassword() {
        return lastFmPassword;
    }

    /**
     * Setter of lastFmPassword.
     *
     * @param lastFmPassword lastFmPassword
     */
    public void setLastFmPassword(String lastFmPassword) {
        this.lastFmPassword = lastFmPassword;
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
