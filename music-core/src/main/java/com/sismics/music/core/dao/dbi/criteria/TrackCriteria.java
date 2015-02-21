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
     * Title.
     */
    private String title;
    
    /**
     * Like.
     */
    private String like;
    
    /**
     * Directory ID.
     */
    private String directoryId;
    
    /**
     * Artist name.
     */
    private String artistName;
    
    /**
     * Random order.
     */
    private Boolean random;
    
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
     * Getter of title.
     *
     * @return title
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Setter of title.
     *
     * @param title title
     * @return Criteria
     */
    public TrackCriteria setTitle(String title) {
        this.title = title;
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
     * Getter of artistName.
     * 
     * @return artistName
     */
    public String getArtistName() {
        return artistName;
    }

    /**
     * Setter of artistName.
     * 
     * @param artistName artistName
     */
    public TrackCriteria setArtistName(String artistName) {
        this.artistName = artistName;
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

    /**
     * Getter of random.
     *
     * @return the random
     */
    public Boolean getRandom() {
        return random;
    }

    /**
     * Setter of random.
     *
     * @param random random
     */
    public TrackCriteria setRandom(Boolean random) {
        this.random = random;
        return this;
    }
}
