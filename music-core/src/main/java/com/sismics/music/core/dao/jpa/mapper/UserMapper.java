package com.sismics.music.core.dao.jpa.mapper;

import com.sismics.music.core.model.jpa.User;
import org.apache.commons.lang.StringUtils;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * User result set mapper.
 *
 * @author jtremeaux
 */
public class UserMapper implements ResultSetMapper<User> {
    private static String[] columns = {
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

    public static String getColumns() {
        return StringUtils.join(columns, ",");
    }

    public static String getColumns(String prefix) {
        String[] prefixedColumns = new String[columns.length];
        for (int i = 0; i < columns.length; i++) {
            prefixedColumns[i] = prefix + "." + columns[i];
        }
        return StringUtils.join(prefixedColumns, ",");
    }

    @Override
    public User map(int index, ResultSet r, StatementContext ctx) throws SQLException {
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
