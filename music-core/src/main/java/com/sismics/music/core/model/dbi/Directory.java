package com.sismics.music.core.model.dbi;

import com.google.common.base.Objects;
import org.apache.commons.lang.StringUtils;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Directory entity.
 * 
 * @author jtremeaux
 */
public class Directory {
    /**
     * Album ID.
     */
    private String id;

    /**
     * Directory title.
     */
    private String name;

    /**
     * Directory location.
     */
    private String location;

    /**
     * Disable date.
     */
    private Date disableDate;
    
    /**
     * Creation date.
     */
    private Date createDate;

    /**
     * Deletion date.
     */
    private Date deleteDate;

    public Directory() {
    }

    public Directory(String id, String name, String location, Date disableDate, Date createDate, Date deleteDate) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.disableDate = disableDate;
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
