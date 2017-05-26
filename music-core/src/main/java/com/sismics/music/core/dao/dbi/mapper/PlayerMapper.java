package com.sismics.music.core.dao.dbi.mapper;

import com.sismics.music.core.model.dbi.Player;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Player result set mapper.
 *
 * @author bgamard
 */
public class PlayerMapper implements ResultSetMapper<Player> {
    @Override
    public Player map(int index, ResultSet r, StatementContext ctx) throws SQLException {
        return new Player(
                r.getString("id"));
    }
}
