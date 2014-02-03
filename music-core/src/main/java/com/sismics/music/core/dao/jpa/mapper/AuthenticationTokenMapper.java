package com.sismics.music.core.dao.jpa.mapper;

import com.sismics.music.core.model.jpa.AuthenticationToken;
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
                r.getString("AUT_ID_C"),
                r.getString("AUT_IDUSER_C"),
                r.getBoolean("AUT_LONGLASTED_B"),
                r.getDate("AUT_CREATEDATE_D"),
                r.getDate("AUT_LASTCONNECTIONDATE_D"));
    }
}
