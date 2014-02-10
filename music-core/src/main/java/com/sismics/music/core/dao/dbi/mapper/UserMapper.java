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
            "USE_ID_C",
            "USE_IDLOCALE_C",
            "USE_IDROLE_C",
            "USE_USERNAME_C",
            "USE_PASSWORD_C",
            "USE_EMAIL_C",
            "USE_THEME_C",
            "USE_MAXBITRATE_N",
            "USE_LASTFMSESSIONTOKEN_C",
            "USE_LASTFMACTIVE_B",
            "USE_FIRSTCONNECTION_B",
            "USE_CREATEDATE_D",
            "USE_DELETEDATE_D"};
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
                r.getString(columns[column++]),
                r.getInt(columns[column++]),
                r.getString(columns[column++]),
                r.getBoolean(columns[column++]),
                r.getBoolean(columns[column++]),
                r.getDate(columns[column++]),
                r.getDate(columns[column++]));
    }
}
