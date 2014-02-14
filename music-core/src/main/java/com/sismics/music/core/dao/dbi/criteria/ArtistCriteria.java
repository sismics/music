package com.sismics.music.core.dao.dbi.criteria;

/**
 * Artist criteria.
 *
 * @author bgamard
 */
public class ArtistCriteria {
    /**
     * Artist ID.
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
    public ArtistCriteria setId(String id) {
        this.id = id;
        return this;
    }
}
