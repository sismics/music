package com.sismics.music.core.dao.jpa.mapper;

import com.sismics.music.core.model.jpa.Track;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Track result set mapper.
 *
 * @author jtremeaux
 */
public class TrackMapper implements ResultSetMapper<Track> {
    @Override
    public Track map(int index, ResultSet r, StatementContext ctx) throws SQLException {
        return new Track(
                r.getString("TRK_ID_C"),
                r.getString("TRK_IDALBUM_C"),
                r.getString("TRK_IDARTIST_C"),
                r.getString("TRK_FILENAME_C"),
                r.getString("TRK_TITLE_C"),
                r.getInt("TRK_YEAR_N"),
                r.getInt("TRK_LENGTH_N"),
                r.getInt("TRK_BITRATE_N"),
                r.getBoolean("TRK_VBR_B"),
                r.getString("TRK_FORMAT_C"),
                r.getDate("TRK_CREATEDATE_D"),
                r.getDate("TRK_DELETEDATE_D"));
    }
}
