package com.sismics.music.core.model.dbi;

import com.google.common.base.Objects;

/**
 * Locale entity.
 * 
 * @author jtremeaux
 */
public class Locale {
    /**
     * Locale ID (ex: fr_FR).
     */
    private String id;

    /**
     * Getter of id.
     *
     * @return id
     */
    public String getId() {
        return id;
    }

    public Locale(String id) {
        this.id = id;
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
