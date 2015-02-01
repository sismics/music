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
                "ALB_ID_C",
                "ALB_IDDIRECTORY_C",
                "ALB_IDARTIST_C",
                "ALB_NAME_C",
                "ALB_ALBUMART_C",
                "ALB_UPDATEDATE_D",
                "ALB_CREATEDATE_D",
                "ALB_DELETEDATE_D",
                "ALB_LOCATION_C"};
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
