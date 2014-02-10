package com.sismics.music.core.model.dbi;

import com.google.common.base.Objects;
import com.sismics.music.core.constant.ConfigType;

/**
 * Configuration parameter entity.
 * 
 * @author jtremeaux
 */
public class Config {
    /**
     * Configuration parameter ID.
     */
    private ConfigType id;
    
    /**
     * Configuration parameter value.
     */
    private String value;

    public Config() {
    }

    public Config(ConfigType id, String value) {
        this.id = id;
        this.value = value;
    }

    /**
     * Getter of id.
     *
     * @return id
     */
    public ConfigType getId() {
        return id;
    }

    /**
     * Setter of id.
     *
     * @param id id
     */
    public void setId(ConfigType id) {
        this.id = id;
    }

    /**
     * Getter of value.
     *
     * @return value
     */
    public String getValue() {
        return value;
    }

    /**
     * Setter of value.
     *
     * @param value value
     */
    public void setValue(String value) {
        this.value = value;
    }
    
    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", id)
                .toString();
    }
}
