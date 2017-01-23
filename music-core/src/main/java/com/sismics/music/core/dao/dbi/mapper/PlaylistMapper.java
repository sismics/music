package com.sismics.music.core.dao.dbi.mapper;

import com.sismics.music.core.dao.dbi.dto.PlaylistDto;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Playlist result set mapper.
 *
 * @author jtremeaux
 */
public class PlaylistMapper implements ResultSetMapper<PlaylistDto> {
    @Override
    public PlaylistDto map(int index, ResultSet r, StatementContext ctx) throws SQLException {
        PlaylistDto dto = new PlaylistDto();
        dto.setId(r.getString("id"));
        dto.setName(r.getString("c0"));
        dto.setUserId(r.getString("userId"));
        dto.setPlaylistTrackCount(r.getLong("c1"));
        dto.setUserTrackPlayCount(r.getLong("c2"));
        return dto;
    }
}
