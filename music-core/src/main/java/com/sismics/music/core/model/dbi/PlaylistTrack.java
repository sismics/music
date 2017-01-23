package com.sismics.music.core.model.dbi;

import com.google.common.base.Objects;
import com.sismics.music.core.dao.dbi.PlaylistTrackDao;

import java.util.UUID;

/**
 * Playlist track entity.
 * 
 * @author jtremeaux
 */
public class PlaylistTrack {
    /**
     * Playlist track ID.
     */
    private String id;

    /**
     * Playlist ID.
     */
    private String playlistId;

    /**
     * Track ID.
     */
    private String trackId;

    /**
     * Order in the playlist.
     */
    private Integer order;

    public PlaylistTrack() {
    }

    public PlaylistTrack(String id, String playlistId, String trackId, Integer order) {
        this.id = id;
        this.playlistId = playlistId;
        this.trackId = trackId;
        this.order = order;
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

    public static void createPlaylistTrack(PlaylistTrack playlistTrack) {
        playlistTrack.setId(UUID.randomUUID().toString());
        new PlaylistTrackDao().create(playlistTrack);
    }
}
