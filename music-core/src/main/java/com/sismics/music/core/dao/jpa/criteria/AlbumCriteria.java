package com.sismics.music.core.dao.jpa.criteria;

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
}
