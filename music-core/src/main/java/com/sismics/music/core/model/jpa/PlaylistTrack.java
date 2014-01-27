package com.sismics.music.core.model.jpa;

import com.google.common.base.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Playlist track entity.
 * 
 * @author jtremeaux
 */
@Entity
@Table(name = "T_PLAYLIST_TRACK")
public class PlaylistTrack {
    /**
     * Playlist track ID.
     */
    @Id
    @Column(name = "PLT_ID_C", length = 36)
    private String id;

    /**
     * Playlist ID.
     */
    @Column(name = "PLT_IDPLAYLIST_C", nullable = false, length = 36)
    private String playlistId;

    /**
     * Track ID.
     */
    @Column(name = "PLT_IDTRACK_C", nullable = false, length = 36)
    private String trackId;

    /**
     * Order in the playlist.
     */
    @Column(name = "PLT_ORDER_N", nullable = false)
    private Integer order;

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
     * Getter of playlistId.
     *
     * @return playlistId
     */
    public String getPlaylistId() {
        return playlistId;
    }

    /**
     * Setter of playlistId.
     *
     * @param playlistId playlistId
     */
    public void setPlaylistId(String playlistId) {
        this.playlistId = playlistId;
    }

    /**
     * Getter of trackId.
     *
     * @return trackId
     */
    public String getTrackId() {
        return trackId;
    }

    /**
     * Setter of trackId.
     *
     * @param trackId trackId
     */
    public void setTrackId(String trackId) {
        this.trackId = trackId;
    }

    /**
     * Getter of order.
     *
     * @return order
     */
    public Integer getOrder() {
        return order;
    }

    /**
     * Setter of order.
     *
     * @param order order
     */
    public void setOrder(Integer order) {
        this.order = order;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", id)
                .add("order", order)
                .add("playlistId", playlistId)
                .add("trackId", trackId)
                .toString();
    }
}
