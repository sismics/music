package com.sismics.music.core.dao.dbi.dto;

/**
 * Artist DTO.
 *
 * @author bgamard
 */
public class ArtistDto {
    /**
     * Artist ID.
     */
    private String id;
    
    /**
     * Album name.
     */
    private String name;
    
    /**
     * Getter of id.
     *
     * @return id
     */
    public String getId() {
        return id;
    }

    /**
     * Setter of id.
     *
     * @param id id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Getter of name.
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Setter of name.
     *
     * @param name name
     */
    public void setName(String name) {
        this.name = name;
    }
}
