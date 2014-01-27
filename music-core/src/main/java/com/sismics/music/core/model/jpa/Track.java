package com.sismics.music.core.model.jpa;

import com.google.common.base.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * Track entity.
 * 
 * @author jtremeaux
 */
@Entity
@Table(name = "T_TRACK")
public class Track {
    /**
     * Track ID.
     */
    @Id
    @Column(name = "TRK_ID_C", length = 36)
    private String id;

    /**
     * Album ID.
     */
    @Column(name = "TRK_IDALBUM_C", nullable = false, length = 36)
    private String albumId;

    /**
     * Artist ID.
     */
    @Column(name = "TRK_IDARTIST_C", nullable = false, length = 36)
    private String artistId;

    /**
     * Track file name.
     */
    @Column(name = "TRK_FILENAME_C", nullable = false, length = 2000)
    private String fileName;
    
    /**
     * Track title.
     */
    @Column(name = "TRK_TITLE_C", nullable = false, length = 2000)
    private String title;

    /**
     * Track year.
     */
    @Column(name = "TRK_YEAR_N")
    private Integer year;
    
    /**
     * Track length (in seconds).
     */
    @Column(name = "TRK_LENGTH_N", nullable = false)
    private Integer length;

    /**
     * Track bitrate (in kbps).
     */
    @Column(name = "TRK_BITRATE_N", nullable = false)
    private Integer bitrate;

    /**
     * Track is encoded in variable bitrate (VBR).
     */
    @Column(name = "TRK_VBR_B", nullable = false)
    private boolean vbr;
    
    /**
     * Track format.
     */
    @Column(name = "TRK_FORMAT_C", nullable = false, length = 10)
    private String format;
    
    /**
     * Creation date.
     */
    @Column(name = "TRK_CREATEDATE_D", nullable = false)
    private Date createDate;
    
    /**
     * Deletion date.
     */
    @Column(name = "TRK_DELETEDATE_D")
    private Date deleteDate;
    
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

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", id)
                .add("title", title)
                .toString();
    }
}
