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
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
