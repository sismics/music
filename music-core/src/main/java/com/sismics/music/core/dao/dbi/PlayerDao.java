package com.sismics.music.core.dao.dbi;

import java.util.UUID;

import org.skife.jdbi.v2.Handle;

import com.sismics.music.core.model.dbi.Player;
import com.sismics.util.context.ThreadLocalContext;

/**
 * Player DAO.
 * 
 * @author bgamard
 */
public class PlayerDao {
    /**
     * Create a player.
     * 
     * @param player Player
     * @return Player ID
     */
    public String create(Player player) {
        player.setId(UUID.randomUUID().toString());

        final Handle handle = ThreadLocalContext.get().getHandle();
        handle.createStatement("insert into " +
                " T_PLAYER(PLR_ID_C)" +
                " values(:id)")
                .bind("id", player.getId())
                .execute();

        return player.getId();
    }
    
    /**
     * Deletes a directory.
     * 
     * @param id Directory ID
     */
    public void delete(String id) {
        final Handle handle = ThreadLocalContext.get().getHandle();
        handle.createStatement("delete from T_PLAYER p" +
                "  where p.PLR_ID_C = :id")
                .bind("id", id)
                .execute();
    }
    
    /**
     * Gets a player by its ID.
     * 
     * @param id Player ID
     * @return Player
     */
    public Player getById(String id) {
        final Handle handle = ThreadLocalContext.get().getHandle();
        return handle.createQuery("select PLR_ID_C from T_PLAYER where PLR_ID_C = :id")
                .bind("id", id)
                .mapTo(Player.class)
                .first();
    }
}
