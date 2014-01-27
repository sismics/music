package com.sismics.music.core.model.jpa;

import com.google.common.base.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * Playlist entity.
 * 
 * @author jtremeaux
 */
@Entity
@Table(name = "T_PLAYLIST")
public class Playlist {
    /**
     * Playlist ID.
     */
    @Id
    @Column(name = "PLL_ID_C", length = 36)
    private String id;

    /**
     * User ID.
     */
    @Column(name = "PLL_IDUSER_C", nullable = false, length = 36)
    private String userId;

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
     * Getter of userId.
     *
     * @return userId
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Setter of userId.
     *
     * @param userId userId
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", id)
                .add("userId", userId)
                .toString();
    }
}
