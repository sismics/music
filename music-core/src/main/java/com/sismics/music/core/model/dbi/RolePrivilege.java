package com.sismics.music.core.model.dbi;

import com.google.common.base.Objects;

import java.util.Date;

/**
 * Role privilege.
 * 
 * @author jtremeaux
 */
public class RolePrivilege {
    /**
     * Role privilege ID.
     */
    private String id;
    
    /**
     * Role ID.
     */
    private String roleId;
    
    /**
     * Privilege ID.
     */
    private String privilegeId;
    
    /**
     * Creation date.
     */
    private Date createDate;
    
    /**
     * Deletion date.
     */
    private Date deleteDate;

    public RolePrivilege() {
    }

    public RolePrivilege(String id, String roleId, String privilegeId, Date createDate, Date deleteDate) {
        this.id = id;
        this.roleId = roleId;
        this.privilegeId = privilegeId;
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
     * Getter of roleId.
     *
     * @return roleId
     */
    public String getRoleId() {
        return roleId;
    }

    /**
     * Setter of roleId.
     *
     * @param roleId roleId
     */
    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    /**
     * Getter of privilegeId.
     *
     * @return privilegeId
     */
    public String getPrivilegeId() {
        return privilegeId;
    }

    /**
     * Setter of privilegeId.
     *
     * @param privilegeId privilegeId
     */
    public void setPrivilegeId(String privilegeId) {
        this.privilegeId = privilegeId;
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
                .add("userId", roleId)
                .add("privilegeId", privilegeId)
                .toString();
    }
}
