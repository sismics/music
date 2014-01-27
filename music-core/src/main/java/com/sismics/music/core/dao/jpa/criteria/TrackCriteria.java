package com.sismics.music.core.dao.jpa.criteria;

/**
 * Track criteria.
 *
 * @author jtremeaux
 */
public class TrackCriteria {
    /**
     * Album ID.
     */
    private String albumId;

    /**
     * Playlist user ID.
     */
    private String userId;

    /**
     * Getter of albumId.
     *
     * @return albumId
     */
    public String getAlbumId() {
        return this.albumId;
    }

    /**
     * Setter of id.
     *
     * @param albumId albumId
     * @return Criteria
     */
    public TrackCriteria setAlbumId(String albumId) {
        this.albumId = albumId;
        return this;
    }
    
    /**
     * Getter of userId.
     *
     * @return userId
     */
    public String getUserId() {
        return this.userId;
    }

    /**
     * Setter of id.
     *
     * @param userId userId
     * @return Criteria
     */
    public TrackCriteria setUserId(String userId) {
        this.userId = userId;
        return this;
    }
}
