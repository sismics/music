package com.sismics.music.core.dao.dbi.mapper;

import com.sismics.music.core.model.dbi.Playlist;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Playlist result set mapper.
 *
 * @author jtremeaux
 */
public class PlaylistMapper implements ResultSetMapper<Playlist> {
    @Override
    public Playlist map(int index, ResultSet r, StatementContext ctx) throws SQLException {
        return new Playlist(
                r.getString("PLL_ID_C"),
                r.getString("PLL_IDUSER_C"));
    }
}
