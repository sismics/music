package com.sismics.music.core.model.dbi;

import com.google.common.base.Objects;

import java.util.Date;

/**
 * User information on an album entity.
 * 
 * @author jtremeaux
 */
public class UserAlbum {
    /**
     * User information on a album ID.
     */
    private String id;

    /**
     * User ID.
     */
    private String userId;

    /**
     * Album ID.
     */
    private String albumId;

    /**
     * User score for this album.
     */
    private Integer score;

    /**
     * Creation date.
     */
    private Date createDate;

    /**
     * Deletion date.
     */
    private Date deleteDate;

    public UserAlbum() {
    }

    public UserAlbum(String id, String userId, String albumId, Integer score, Date createDate, Date deleteDate) {
        this.id = id;
        this.userId = userId;
        this.albumId = albumId;
        this.score = score;
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
     * Getter of albumId.
     *
     * @return albumId
     */
    public String getAlbumId() {
        return albumId;
    }

    /**
     * Setter of albumId.
     *
     * @param albumId albumId
     */
    public void setAlbumId(String albumId) {
        this.albumId = albumId;
    }

    /**
     * Getter of score.
     *
     * @return score
     */
    public Integer getScore() {
        return score;
    }

    /**
     * Setter of score.
     *
     * @param score score
     */
    public void setScore(Integer score) {
        this.score = score;
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
                .add("userId", userId)
                .add("albumId", albumId)
                .toString();
    }
}
