package com.sismics.music.core.dao.dbi.mapper;

import com.sismics.music.core.model.dbi.Track;
import com.sismics.util.dbi.BaseResultSetMapper;
import org.skife.jdbi.v2.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Track result set mapper.
 *
 * @author jtremeaux
 */
public class TrackMapper extends BaseResultSetMapper<Track> {
    public String[] getColumns() {
        return new String[] {
                "TRK_ID_C",
                "TRK_IDALBUM_C",
                "TRK_IDARTIST_C",
                "TRK_FILENAME_C",
                "TRK_TITLE_C",
                "TRK_TITLECORRECTED_C",
                "TRK_YEAR_N",
                "TRK_GENRE_C",
                "TRK_LENGTH_N",
                "TRK_BITRATE_N",
                "TRK_ORDER_N",
                "TRK_VBR_B",
                "TRK_FORMAT_C",
                "TRK_CREATEDATE_D",
                "TRK_DELETEDATE_D"};
    }

    @Override
    public Track map(int index, ResultSet r, StatementContext ctx) throws SQLException {
        final String[] columns = getColumns();
        int column = 0;
        return new Track(
                r.getString(columns[column++]),
                r.getString(columns[column++]),
                r.getString(columns[column++]),
                r.getString(columns[column++]),
                r.getString(columns[column++]),
                r.getString(columns[column++]),
                r.getInt(columns[column++]),
                r.getString(columns[column++]),
                r.getInt(columns[column++]),
                r.getInt(columns[column++]),
                r.getInt(columns[column++]),
                r.getBoolean(columns[column++]),
                r.getString(columns[column++]),
                r.getTimestamp(columns[column++]),
                r.getTimestamp(columns[column++]));
    }
}
