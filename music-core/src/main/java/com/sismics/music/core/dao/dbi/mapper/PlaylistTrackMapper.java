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
                r.getString("id"),
                r.getString("playlist_id"),
                r.getString("track_id"),
                r.getInt("number"));
    }
}
