package com.sismics.music.core.dao.dbi.mapper;

import com.sismics.music.core.dao.dbi.dto.UserDto;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Album result set mapper.
 *
 * @author jtremeaux
 */
public class UserDtoMapper implements ResultSetMapper<UserDto> {
    @Override
    public UserDto map(int index, ResultSet r, StatementContext ctx) throws SQLException {
        UserDto dto = new UserDto();
        dto.setId(r.getString("c0"));
        dto.setUsername(r.getString("c1"));
        dto.setEmail(r.getString("c2"));
        dto.setCreateTimestamp(r.getTimestamp("c3").getTime());
        dto.setLocaleId(r.getString("c4"));

        return dto;
    }
}
