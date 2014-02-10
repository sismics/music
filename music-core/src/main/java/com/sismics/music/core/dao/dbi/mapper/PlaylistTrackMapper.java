package com.sismics.music.core.dao.dbi.mapper;

import com.sismics.music.core.model.dbi.PlaylistTrack;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Playlisttrack result set mapper.
 *
 * @author jtremeaux
 */
public class PlaylistTrackMapper implements ResultSetMapper<PlaylistTrack> {
    @Override
    public PlaylistTrack map(int index, ResultSet r, StatementContext ctx) throws SQLException {
        return new PlaylistTrack(
                r.getString("PLT_ID_C"),
                r.getString("PLT_IDPLAYLIST_C"),
                r.getString("PLT_IDTRACK_C"),
                r.getInt("PLT_ORDER_N"));
    }
}
