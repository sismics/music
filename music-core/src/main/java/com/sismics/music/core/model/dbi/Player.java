package com.sismics.music.core.model.dbi;

import com.google.common.base.Objects;

/**
 * Player.
 * 
 * @author bgamard
 */
public class Player {
    /**
     * Player ID.
     */
    private String id;
    
    public Player() {
    }

    public Player(String id) {
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
