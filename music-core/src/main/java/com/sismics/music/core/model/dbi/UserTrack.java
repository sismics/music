package com.sismics.music.core.model.dbi;

import com.google.common.base.Objects;

import java.util.Date;

/**
 * User information on a track entity.
 * 
 * @author jtremeaux
 */
public class UserTrack {
    /**
     * User information on a track ID.
     */
    private String id;

    /**
     * User ID.
     */
    private String userId;

    /**
     * Track ID.
     */
    private String trackId;

    /**
     * Number of times this track was played.
     */
    private Integer playCount;

    /**
     * True if this track is a like.
     */
    private boolean like;

    /**
     * Creation date.
     */
    private Date createDate;

    /**
     * Deletion date.
     */
    private Date deleteDate;

    public UserTrack() {
    }

    public UserTrack(String id, String userId, String trackId, Integer playCount, Boolean like, Date createDate, Date deleteDate) {
        this.id = id;
        this.userId = userId;
        this.trackId = trackId;
        this.playCount = playCount;
        this.like = like;
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
     * Getter of trackId.
     *
     * @return trackId
     */
    public String getTrackId() {
        return trackId;
    }

    /**
     * Setter of trackId.
     *
     * @param trackId trackId
     */
    public void setTrackId(String trackId) {
        this.trackId = trackId;
    }

    /**
     * Getter of playCount.
     *
     * @return playCount
     */
    public Integer getPlayCount() {
        return playCount;
    }

    /**
     * Setter of playCount.
     *
     * @param playCount playCount
     */
    public void setPlayCount(Integer playCount) {
        this.playCount = playCount;
    }

    /**
     * Getter of like.
     *
     * @return like
     */
    public boolean isLike() {
        return like;
    }

    /**
     * Setter of like.
     *
     * @param like like
     */
    public void setLike(boolean like) {
        this.like = like;
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
                .add("trackId", trackId)
                .toString();
    }
}
