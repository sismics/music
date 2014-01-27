package com.sismics.music.core.model.jpa;

import com.google.common.base.Objects;
import org.apache.commons.lang.StringUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Directory entity.
 * 
 * @author jtremeaux
 */
@Entity
@Table(name = "T_DIRECTORY")
public class Directory {
    /**
     * Album ID.
     */
    @Id
    @Column(name = "DIR_ID_C", length = 36)
    private String id;

    /**
     * Directory title.
     */
    @Column(name = "DIR_NAME_C", nullable = false, length = 100)
    private String name;

    /**
     * Directory location.
     */
    @Column(name = "DIR_LOCATION_C", nullable = false, length = 1000)
    private String location;

    /**
     * Disable date.
     */
    @Column(name = "DIR_DISABLEDATE_D")
    private Date disableDate;
    
    /**
     * Creation date.
     */
    @Column(name = "DIR_CREATEDATE_D", nullable = false)
    private Date createDate;

    /**
     * Deletion date.
     */
    @Column(name = "DIR_DELETEDATE_D")
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
     * Getter of location.
     *
     * @return location
     */
    public String getLocation() {
        return location;
    }

    /**
     * Setter of location.
     *
     * @param location location
     */
    public void setLocation(String location) {
        this.location = location;
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
     * Getter of disableDate.
     *
     * @return disableDate
     */
    public Date getDisableDate() {
        return disableDate;
    }

    /**
     * Setter of disableDate.
     *
     * @param disableDate disableDate
     */
    public void setDisableDate(Date disableDate) {
        this.disableDate = disableDate;
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
     * Normalize the directory location.
     */
    public void normalizeLocation() {
        location.replaceAll("\\\\", "/");
    }

    /**
     * Infers the directory name from the location.
     */
    public void updateNameFromLocation() {
        if (location == null) {
            name = null;
            return;
        }
        Pattern p = Pattern.compile("(.+/)?(.+)");
        Matcher m = p.matcher(location);
        if (m.matches()) {
            name = m.group(2);
            if (name.startsWith("/")) {
                name = name.substring(1);
            }
            if (name.endsWith("/")) {
                name = name.substring(0, name.length() - 1);
            }
        }
        if (StringUtils.isBlank(name)) {
            name = "music";
        }
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", id)
                .add("name", name)
                .add("location", location)
                .toString();
    }
}
