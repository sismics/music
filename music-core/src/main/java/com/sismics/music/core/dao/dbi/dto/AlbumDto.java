package com.sismics.music.core.dao.dbi.dto;

import java.util.Date;

/**
 * Album DTO.
 *
 * @author jtremeaux 
 */
public class AlbumDto {
    /**
     * Album ID.
     */
    private String id;
    
    /**
     * Album name.
     */
    private String name;
    
    /**
     * Album art ID.
     */
    private String albumArt;

    /**
     * Artist ID.
     */
    private String artistId;
    
    /**
     * Artist name.
     */
    private String artistName;
    
    /**
     * Last update date.
     */
    private Date updateDate;
    
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

    /**
     * Getter of albumArt.
     *
     * @return albumArt
     */
    public String getAlbumArt() {
        return albumArt;
    }

    /**
     * Setter of albumArt.
     *
     * @param albumArt albumArt
     */
    public void setAlbumArt(String albumArt) {
        this.albumArt = albumArt;
    }

    /**
     * Getter of artistId.
     *
     * @return artistId
     */
    public String getArtistId() {
        return artistId;
    }

    /**
     * Setter of artistId.
     *
     * @param artistId artistId
     */
    public void setArtistId(String artistId) {
        this.artistId = artistId;
    }

    /**
     * Getter of artistName.
     *
     * @return artistName
     */
    public String getArtistName() {
        return artistName;
    }

    /**
     * Setter of artistName.
     *
     * @param artistName artistName
     */
    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    /**
     * Getter of updateDate.
     *
     * @return the updateDate
     */
    public Date getUpdateDate() {
        return updateDate;
    }

    /**
     * Setter of updateDate.
     *
     * @param updateDate updateDate
     */
    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }
}
