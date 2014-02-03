package com.sismics.music.core.model.jpa;

import com.google.common.base.Objects;

/**
 * Base function entity.
 * 
 * @author jtremeaux
 */
public class BaseFunction {
    /**
     * Base function ID (ex: "ADMIN").
     */
    private String id;

    public BaseFunction(String id) {
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
