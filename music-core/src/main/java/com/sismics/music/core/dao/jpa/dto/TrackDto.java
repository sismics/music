package com.sismics.music.core.dao.jpa.dto;


/**
 * Track DTO.
 *
 * @author jtremeaux 
 */
public class TrackDto {
    /**
     * Track ID.
     */
    private String id;
    
    /**
     * Track filename.
     */
    private String fileName;
    
    /**
     * Track title.
     */
    private String title;
    
    /**
     * Track year.
     */
    private Integer year;
    
    /**
     * Track length (in seconds).
     */
    private Integer length;
    
    /**
     * Track bitrate (in kbps).
     */
    private Integer bitrate;

    /**
     * Track is encoded in variable bitrate (VBR).
     */
    private boolean vbr;

    /**
     * Track format.
     */
    private String format;

    /**
     * Artist id.
     */
    private String artistId;

    /**
     * Artist name.
     */
    private String artistName;
    
    /**
     * Album id.
     */
    private String albumId;

    /**
     * Album name.
     */
    private String albumName;

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
     * Getter of fileName.
     *
     * @return fileName
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Setter of fileName.
     *
     * @param fileName fileName
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Getter of title.
     *
     * @return title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Setter of title.
     *
     * @param title title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Getter of year.
     *
     * @return year
     */
    public Integer getYear() {
        return year;
    }

    /**
     * Setter of year.
     *
     * @param year year
     */
    public void setYear(Integer year) {
        this.year = year;
    }

    /**
     * Getter of length.
     *
     * @return length
     */
    public Integer getLength() {
        return length;
    }

    /**
     * Setter of length.
     *
     * @param length length
     */
    public void setLength(Integer length) {
        this.length = length;
    }

    /**
     * Getter of bitrate.
     *
     * @return bitrate
     */
    public Integer getBitrate() {
        return bitrate;
    }

    /**
     * Setter of bitrate.
     *
     * @param bitrate bitrate
     */
    public void setBitrate(Integer bitrate) {
        this.bitrate = bitrate;
    }

    /**
     * Getter of vbr.
     *
     * @return vbr
     */
    public boolean isVbr() {
        return vbr;
    }

    /**
     * Setter of vbr.
     *
     * @param vbr vbr
     */
    public void setVbr(boolean vbr) {
        this.vbr = vbr;
    }

    /**
     * Getter of format.
     *
     * @return format
     */
    public String getFormat() {
        return format;
    }

    /**
     * Setter of format.
     *
     * @param format format
     */
    public void setFormat(String format) {
        this.format = format;
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
     * Getter of albumId.
     *
     * @return the albumId
     */
    public String getAlbumId() {
        return albumId;
    }

    /**
     * Setter of albumId.
     *
     * @param albumId albumId
     */
    public void setAlbumId(String albumId) {
        this.albumId = albumId;
    }

    /**
     * Getter of albumName.
     *
     * @return the albumName
     */
    public String getAlbumName() {
        return albumName;
    }

    /**
     * Setter of albumName.
     *
     * @param albumName albumName
     */
    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }
}
