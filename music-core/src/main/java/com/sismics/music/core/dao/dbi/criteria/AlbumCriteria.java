package com.sismics.music.core.dao.dbi.criteria;

/**
 * Album criteria.
 *
 * @author jtremeaux
 */
public class AlbumCriteria {
    /**
     * Album ID.
     */
    private String id;

    /**
     * Directory ID.
     */
    private String directoryId;
    
    /**
     * Album name (like).
     */
    private String nameLike;
    
    /**
     * Artist ID.
     */
    private String artistId;

    /**
     * User ID.
     */
    private String userId;
    
    /**
     * Getter of id.
     *
     * @return id
     */
    public String getId() {
        return this.id;
    }

    /**
     * Setter of id.
     *
     * @param id id
     * @return Criteria
     */
    public AlbumCriteria setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Getter of directoryId.
     *
     * @return directoryId
     */
    public String getDirectoryId() {
        return this.directoryId;
    }

    /**
     * Setter of directoryId.
     *
     * @param directoryId directoryId
     * @return Criteria
     */
    public AlbumCriteria setDirectoryId(String directoryId) {
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
     * @return Criteria
     */
    public AlbumCriteria setArtistId(String artistId) {
        this.artistId = artistId;
        return this;
    }

    /**
     * Getter of nameLike.
     * @return nameLike
     */
    public String getNameLike() {
        return nameLike;
    }

    /**
     * Setter of nameLike.
     * @param nameLike nameLike
     * @return Criteria
     */
    public AlbumCriteria setNameLike(String nameLike) {
        this.nameLike = nameLike;
        return this;
    }

    /**
     * Getter of userId.
     *
     * @return the userId
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Setter of userId.
     *
     * @param userId userId
     */
    public AlbumCriteria setUserId(String userId) {
        this.userId = userId;
        return this;
    }
}
