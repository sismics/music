package com.sismics.music.core.dao.dbi;

import com.sismics.music.core.model.dbi.Player;
import com.sismics.util.context.ThreadLocalContext;
import org.skife.jdbi.v2.Handle;

import java.util.UUID;

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
                " t_player(id)" +
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
        handle.createStatement("delete from t_player p" +
                "  where p.id = :id")
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
        return handle.createQuery("select id from t_player where id = :id")
                .bind("id", id)
                .mapTo(Player.class)
                .first();
    }
}
