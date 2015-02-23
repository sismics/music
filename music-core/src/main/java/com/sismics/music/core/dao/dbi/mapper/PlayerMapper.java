package com.sismics.music.core.dao.dbi.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import com.sismics.music.core.model.dbi.Player;

/**
 * Player result set mapper.
 *
 * @author bgamard
 */
public class PlayerMapper implements ResultSetMapper<Player> {
    @Override
    public Player map(int index, ResultSet r, StatementContext ctx) throws SQLException {
        return new Player(
                r.getString("PLR_ID_C"));
    }
}
