package com.sismics.music.core.dao.dbi.dto;


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
     * Track genre.
     */
    private String genre;
    
    /**
     * Track length (in seconds).
     */
    private Integer length;
    
    /**
     * Track bitrate (in kbps).
     */
    private Integer bitrate;
    
    /**
     * Track order.
     */
    private Integer order;

    /**
     * Track is encoded in variable bitrate (VBR).
     */
    private boolean vbr;

    /**
     * Number of times this track was played.
     */
    private Integer userTrackPlayCount;

    /**
     * True if this track is a like.
     */
    private boolean userTrackLike;

    /**
     *  format.
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
     * Album art ID.
     */
    private String albumArt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    public Integer getBitrate() {
        return bitrate;
    }

    public void setBitrate(Integer bitrate) {
        this.bitrate = bitrate;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public boolean isVbr() {
        return vbr;
    }

    public void setVbr(boolean vbr) {
        this.vbr = vbr;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public Integer getUserTrackPlayCount() {
        return userTrackPlayCount;
    }

    public void setUserTrackPlayCount(Integer userTrackPlayCount) {
        this.userTrackPlayCount = userTrackPlayCount;
    }

    public boolean isUserTrackLike() {
        return userTrackLike;
    }

    public void setUserTrackLike(boolean userTrackLike) {
        this.userTrackLike = userTrackLike;
    }

    public String getArtistId() {
        return artistId;
    }

    public void setArtistId(String artistId) {
        this.artistId = artistId;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getAlbumId() {
        return albumId;
    }

    public void setAlbumId(String albumId) {
        this.albumId = albumId;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public String getAlbumArt() {
        return albumArt;
    }

    public void setAlbumArt(String albumArt) {
        this.albumArt = albumArt;
    }
}
