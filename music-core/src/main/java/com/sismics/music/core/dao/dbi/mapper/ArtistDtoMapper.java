package com.sismics.music.core.dao.dbi.mapper;

import com.sismics.music.core.dao.dbi.dto.ArtistDto;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Artist result set mapper.
 *
 * @author jtremeaux
 */
public class ArtistDtoMapper implements ResultSetMapper<ArtistDto> {
    @Override
    public ArtistDto map(int index, ResultSet r, StatementContext ctx) throws SQLException {
        ArtistDto dto = new ArtistDto();
        dto.setId(r.getString("id"));
        dto.setName(r.getString("c0"));
        return dto;
    }
}
