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
    
    public String getAlbumId() {
        return this.albumId;
    }

    public TrackCriteria setAlbumId(String albumId) {
        this.albumId = albumId;
        return this;
    }
    
    public String getUserId() {
        return this.userId;
    }

    public TrackCriteria setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public String getPlaylistId() {
        return this.playlistId;
    }

    public TrackCriteria setPlaylistId(String playlistId) {
        this.playlistId = playlistId;
        return this;
    }

    public String getTitle() {
        return this.title;
    }

    public TrackCriteria setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getLike() {
        return this.like;
    }

    public TrackCriteria setLike(String like) {
        this.like = like;
        return this;
    }

    public String getDirectoryId() {
        return directoryId;
    }

    public TrackCriteria setDirectoryId(String directoryId) {
        this.directoryId = directoryId;
        return this;
    }

    public String getArtistName() {
        return artistName;
    }

    public TrackCriteria setArtistName(String artistName) {
        this.artistName = artistName;
        return this;
    }

    public String getArtistId() {
        return artistId;
    }

    public TrackCriteria setArtistId(String artistId) {
        this.artistId = artistId;
        return this;
    }

    public Boolean getRandom() {
        return random;
    }

    public TrackCriteria setRandom(Boolean random) {
        this.random = random;
        return this;
    }
}
