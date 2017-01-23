package com.sismics.music.core.dao.dbi.mapper;

import com.sismics.music.core.dao.dbi.dto.AlbumDto;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Album result set mapper.
 *
 * @author jtremeaux
 */
public class AlbumDtoMapper implements ResultSetMapper<AlbumDto> {
    @Override
    public AlbumDto map(int index, ResultSet r, StatementContext ctx) throws SQLException {
        AlbumDto dto = new AlbumDto();
        dto.setId(r.getString("id"));
        dto.setName(r.getString("c0"));
        dto.setAlbumArt(r.getString("albumArt"));
        dto.setArtistId(r.getString("artistId"));
        dto.setArtistName(r.getString("artistName"));
        dto.setUpdateDate(r.getTimestamp("c1"));
        dto.setUserPlayCount(r.getLong("c2"));
        return dto;
    }
}
