package com.sismics.music.core.dao.dbi.mapper;

import com.sismics.music.core.model.dbi.Album;
import com.sismics.util.dbi.BaseResultSetMapper;
import org.skife.jdbi.v2.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Album result set mapper.
 *
 * @author jtremeaux
 */
public class AlbumMapper extends BaseResultSetMapper<Album> {
    public String[] getColumns() {
        return new String[] {
                "id",
                "directory_id",
                "artist_id",
                "name",
                "albumart",
                "updatedate",
                "createdate",
                "deletedate",
                "location"};
    }

    @Override
    public Album map(int index, ResultSet r, StatementContext ctx) throws SQLException {
        final String[] columns = getColumns();
        int column = 0;
        return new Album(
                r.getString(columns[column++]),
                r.getString(columns[column++]),
                r.getString(columns[column++]),
                r.getString(columns[column++]),
                r.getString(columns[column++]),
                r.getTimestamp(columns[column++]),
                r.getTimestamp(columns[column++]),
                r.getTimestamp(columns[column++]),
                r.getString(columns[column]));
    }
}
