package com.sismics.music.core.dao.dbi.criteria;

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
     * Artist ID.
     */
    private String artistId;

    /**
     * Playlist user ID.
     */
    private String userId;

    /**
     * Playlist ID.
     */
    private String playlistId;

    /**
     * Like.
     */
    private String like;
    
    /**
     * Directory ID.
     */
    private String directoryId;
    
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

    /**
    /**
     * Getter of playlistId.
     *
     * @return playlistId
     */
    public String getPlaylistId() {
        return this.playlistId;
    }

    /**
     * Setter of id.
     *
     * @param playlistId playlistId
     * @return Criteria
     */
    public TrackCriteria setPlaylistId(String playlistId) {
        this.playlistId = playlistId;
        return this;
    }
    
    /**
     * Getter of like.
     *
     * @return like
     */
    public String getLike() {
        return this.like;
    }

    /**
     * Setter of like.
     *
     * @param like like
     * @return Criteria
     */
    public TrackCriteria setLike(String like) {
        this.like = like;
        return this;
    }

    /**
     * Getter of directoryId.
     * 
     * @return directoryId
     */
    public String getDirectoryId() {
        return directoryId;
    }

    /**
     * Setter of directoryId.
     * 
     * @param directoryId directoryId
     */
    public TrackCriteria setDirectoryId(String directoryId) {
        this.directoryId = directoryId;
        return this;
    }

    /**
     * Getter of artistId.
     *
     * @return the artistId
     */
    public String getArtistId() {
        return artistId;
    }

    /**
     * Setter of artistId.
     *
     * @param artistId artistId
     */
    public TrackCriteria setArtistId(String artistId) {
        this.artistId = artistId;
        return this;
    }
}
