package com.sismics.music.core.dao.dbi.mapper;

import com.sismics.music.core.model.dbi.AuthenticationToken;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Album result set mapper.
 *
 * @author jtremeaux
 */
public class AuthenticationTokenMapper implements ResultSetMapper<AuthenticationToken> {
    @Override
    public AuthenticationToken map(int index, ResultSet r, StatementContext ctx) throws SQLException {
        return new AuthenticationToken(
                r.getString("id"),
                r.getString("user_id"),
                r.getBoolean("longlasted"),
                r.getTimestamp("createdate"),
                r.getTimestamp("lastconnectiondate"));
    }
}
