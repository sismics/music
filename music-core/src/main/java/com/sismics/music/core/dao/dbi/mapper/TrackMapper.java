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
                "id",
                "album_id",
                "artist_id",
                "filename",
                "title",
                "titlecorrected",
                "year",
                "genre",
                "length",
                "bitrate",
                "number",
                "vbr",
                "format",
                "createdate",
                "deletedate"};
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
