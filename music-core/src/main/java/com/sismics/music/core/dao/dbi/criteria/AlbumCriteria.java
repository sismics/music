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
}
