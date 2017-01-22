package com.sismics.music.core.dao.dbi.dto;

/**
 * Playlist DTO.
 *
 * @author jtremeaux
 */
public class PlaylistDto {
    /**
     * Playlist ID.
     */
    private String id;
    
    /**
     * Playlist name.
     */
    private String name;
    
    /**
     * Owner user ID.
     */
    private String userId;

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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
