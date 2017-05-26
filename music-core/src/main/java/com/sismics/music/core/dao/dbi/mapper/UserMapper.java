package com.sismics.music.core.dao.dbi.mapper;

import com.sismics.music.core.model.dbi.User;
import com.sismics.util.dbi.BaseResultSetMapper;
import org.skife.jdbi.v2.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * User result set mapper.
 *
 * @author jtremeaux
 */
public class UserMapper extends BaseResultSetMapper<User> {
    public String[] getColumns() {
        return new String[] {
            "id",
            "locale_id",
            "role_id",
            "username",
            "password",
            "email",
            "maxbitrate",
            "lastfmsessiontoken",
            "lastfmactive",
            "firstconnection",
            "createdate",
            "deletedate"};
    }

    @Override
    public User map(int index, ResultSet r, StatementContext ctx) throws SQLException {
        final String[] columns = getColumns();
        int column = 0;
        return new User(
                r.getString(columns[column++]),
                r.getString(columns[column++]),
                r.getString(columns[column++]),
                r.getString(columns[column++]),
                r.getString(columns[column++]),
                r.getString(columns[column++]),
                r.getInt(columns[column++]),
                r.getString(columns[column++]),
                r.getBoolean(columns[column++]),
                r.getBoolean(columns[column++]),
                r.getTimestamp(columns[column++]),
                r.getTimestamp(columns[column++]));
    }
}
