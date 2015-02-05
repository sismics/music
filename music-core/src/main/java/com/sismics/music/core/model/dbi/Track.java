package com.sismics.music.core.model.dbi;

import com.google.common.base.Objects;

import java.util.Date;

/**
 * Track entity.
 * 
 * @author jtremeaux
 */
public class Track {
    /**
     * Track ID.
     */
    private String id;

    /**
     * Album ID.
     */
    private String albumId;

    /**
     * Artist ID.
     */
    private String artistId;

    /**
     * Track file name.
     */
    private String fileName;
    
    /**
     * Track title.
     */
    private String title;

    /**
     * Title corrected.
     */
    private String titleCorrected;
    
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
     * Track format.
     */
    private String format;

    /**
     * Creation date.
     */
    private Date createDate;
    
    /**
     * Deletion date.
     */
    private Date deleteDate;

    public Track() {
    }

    public Track(String id, String albumId, String artistId, String fileName, String title, String titleCorrected, Integer year, String genre, Integer length, Integer bitrate, Integer order, boolean vbr, String format, Date createDate, Date deleteDate) {
        this.id = id;
        this.albumId = albumId;
        this.artistId = artistId;
        this.fileName = fileName;
        this.title = title;
        this.titleCorrected = titleCorrected;
        this.year = year;
        this.genre = genre;
        this.length = length;
        this.bitrate = bitrate;
        this.order = order;
        this.vbr = vbr;
        this.format = format;
        this.createDate = createDate;
        this.deleteDate = deleteDate;
    }

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
     * Getter of albumId.
     *
     * @return albumId
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
     * Getter of genre.
     *
     * @return the genre
     */
    public String getGenre() {
        return genre;
    }

    /**
     * Setter of genre.
     *
     * @param genre genre
     */
    public void setGenre(String genre) {
        this.genre = genre;
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
     * Getter of order.
     * @return order
     */
    public Integer getOrder() {
        return order;
    }

    /**
     * Setter of order.
     * @param order order
     */
    public void setOrder(Integer order) {
        this.order = order;
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
     * Getter of createDate.
     *
     * @return createDate
     */
    public Date getCreateDate() {
        return createDate;
    }

    /**
     * Setter of createDate.
     *
     * @param createDate createDate
     */
    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    /**
     * Getter of deleteDate.
     *
     * @return deleteDate
     */
    public Date getDeleteDate() {
        return deleteDate;
    }

    /**
     * Setter of deleteDate.
     *
     * @param deleteDate deleteDate
     */
    public void setDeleteDate(Date deleteDate) {
        this.deleteDate = deleteDate;
    }

    /**
     * Getter of titleCorrected.
     *
     * @return the titleCorrected
     */
    public String getTitleCorrected() {
        return titleCorrected;
    }

    /**
     * Setter of titleCorrected.
     *
     * @param titleCorrected titleCorrected
     */
    public void setTitleCorrected(String titleCorrected) {
        this.titleCorrected = titleCorrected;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", id)
                .add("title", title)
                .toString();
    }
}
