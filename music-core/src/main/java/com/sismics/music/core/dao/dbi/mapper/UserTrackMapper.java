package com.sismics.music.core.dao.dbi.mapper;

import com.sismics.music.core.model.dbi.UserTrack;
import com.sismics.util.dbi.BaseResultSetMapper;
import org.skife.jdbi.v2.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * User / track result set mapper.
 *
 * @author jtremeaux
 */
public class UserTrackMapper extends BaseResultSetMapper<UserTrack> {
    public String[] getColumns() {
        return new String[] {
            "id",
            "user_id",
            "track_id",
            "playcount",
            "liked",
            "createdate",
            "deletedate"};
    }

    @Override
    public UserTrack map(int index, ResultSet r, StatementContext ctx) throws SQLException {
        final String[] columns = getColumns();
        int column = 0;
        return new UserTrack(
                r.getString(columns[column++]),
                r.getString(columns[column++]),
                r.getString(columns[column++]),
                r.getInt(columns[column++]),
                r.getBoolean(columns[column++]),
                r.getTimestamp(columns[column++]),
                r.getTimestamp(columns[column++]));
    }
}
