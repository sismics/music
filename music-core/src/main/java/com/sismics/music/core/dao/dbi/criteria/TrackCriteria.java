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
     * Artist name.
     */
    private String artistName;

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
     * Title (like).
     */
    private String titleLike;
    
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
     * Getter of artistName.
     *
     * @return artistName
     */
    public String getArtistName() {
        return this.artistName;
    }

    /**
     * Setter of id.
     *
     * @param artistName artistName
     * @return Criteria
     */
    public TrackCriteria setArtistName(String artistName) {
        this.artistName = artistName;
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
     * Setter of id.
     *
     * @param title title
     * @return Criteria
     */
    public TrackCriteria setTitle(String title) {
        this.title = title;
        return this;
    }
    
    /**
     * Getter of titleLike.
     *
     * @return titleLike
     */
    public String getTitleLike() {
        return this.titleLike;
    }

    /**
     * Setter of titleLike.
     *
     * @param titleLike titleLike
     * @return Criteria
     */
    public TrackCriteria setTitleLike(String titleLike) {
        this.titleLike = titleLike;
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
    
    
}
