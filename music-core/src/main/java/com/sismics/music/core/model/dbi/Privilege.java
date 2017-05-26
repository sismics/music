package com.sismics.music.core.model.dbi;

import com.google.common.base.Objects;

/**
 * Privilege entity.
 * 
 * @author jtremeaux
 */
public class Privilege {
    /**
     * Privilege ID (ex: "ADMIN").
     */
    private String id;

    public Privilege(String id) {
        this.id = id;
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
    
    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", id)
                .toString();
    }
}
