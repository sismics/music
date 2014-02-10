package com.sismics.music.core.dao.dbi;

import com.sismics.music.core.model.dbi.Playlist;
import com.sismics.util.context.ThreadLocalContext;
import org.skife.jdbi.v2.Handle;

import java.util.UUID;

/**
 * Playlist DAO.
 * 
 * @author jtremeaux
 */
public class PlaylistDao {
    /**
     * Creates a new playlist.
     * 
     * @param playlist Playlist to create
     * @return Playlist ID
     */
    public String create(Playlist playlist) {
        playlist.setId(UUID.randomUUID().toString());

        final Handle handle = ThreadLocalContext.get().getHandle();
        handle.createStatement("insert into " +
                "  T_PLAYLIST(PLL_ID_C, PLL_IDUSER_C)" +
                "  values(:id, :userId)")
                .bind("id", playlist.getId())
                .bind("userId", playlist.getUserId())
                .execute();

        return playlist.getId();
    }

    /**
     * Gets a playlist by user ID.
     *
     * @param userId User ID
     * @return Playlist
     */
    public Playlist getActiveByUserId(String userId) {
        final Handle handle = ThreadLocalContext.get().getHandle();
        return handle.createQuery("select p.PLL_ID_C, p.PLL_IDUSER_C" +
                "  from T_PLAYLIST p" +
                "  where p.PLL_IDUSER_C = :userId ")
                .bind("userId", userId)
                .mapTo(Playlist.class)
                .first();
    }
}
