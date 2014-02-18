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
     * Artist name (like).
     */
    private String nameLike;

    /**
     * Getter of id.
     *
     * @return id
     */
    public String getId() {
        return this.id;
    }

    /**
     * Getter of nameLike.
     *
     * @return the nameLike
     */
    public String getNameLike() {
        return nameLike;
    }

    /**
     * Setter of nameLike.
     *
     * @param nameLike nameLike
     * @return Criteria
     */
    public ArtistCriteria setNameLike(String nameLike) {
        this.nameLike = nameLike;
        return this;
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
