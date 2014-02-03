package com.sismics.music.core.model.jpa;

import com.google.common.base.Objects;

import java.util.Date;

/**
 * Role base function.
 * 
 * @author jtremeaux
 */
public class RoleBaseFunction {
    /**
     * Role base function ID.
     */
    private String id;
    
    /**
     * Role ID.
     */
    private String roleId;
    
    /**
     * Base function ID.
     */
    private String baseFunctionId;
    
    /**
     * Creation date.
     */
    private Date createDate;
    
    /**
     * Deletion date.
     */
    private Date deleteDate;

    public RoleBaseFunction() {
    }

    public RoleBaseFunction(String id, String roleId, String baseFunctionId, Date createDate, Date deleteDate) {
        this.id = id;
        this.roleId = roleId;
        this.baseFunctionId = baseFunctionId;
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
     * Getter of baseFunctionId.
     *
     * @return baseFunctionId
     */
    public String getBaseFunctionId() {
        return baseFunctionId;
    }

    /**
     * Setter of baseFunctionId.
     *
     * @param baseFunctionId baseFunctionId
     */
    public void setBaseFunctionId(String baseFunctionId) {
        this.baseFunctionId = baseFunctionId;
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
                .add("baseFunctionId", baseFunctionId)
                .toString();
    }
}
