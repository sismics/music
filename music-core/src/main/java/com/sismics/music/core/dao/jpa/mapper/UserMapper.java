package com.sismics.music.core.dao.jpa.mapper;

import com.sismics.music.core.model.jpa.User;
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
    @Override
    public User map(int index, ResultSet r, StatementContext ctx) throws SQLException {
        return new User(
                r.getString("USE_ID_C"),
                r.getString("USE_IDLOCALE_C"),
                r.getString("USE_IDROLE_C"),
                r.getString("USE_USERNAME_C"),
                r.getString("USE_PASSWORD_C"),
                r.getString("USE_EMAIL_C"),
                r.getString("USE_THEME_C"),
                r.getInt("USE_MAXBITRATE_N"),
                r.getString("USE_LASTFMLOGIN_C"),
                r.getString("USE_LASTFMPASSWORD_C"),
                r.getBoolean("USE_LASTFMACTIVE_B"),
                r.getBoolean("USE_FIRSTCONNECTION_B"),
                r.getDate("USE_CREATEDATE_D"),
                r.getDate("USE_DELETEDATE_D"));
    }
}
