package com.sismics.music.core.dao.dbi.mapper;

import com.sismics.music.core.model.dbi.Artist;
import com.sismics.util.dbi.BaseResultSetMapper;
import org.skife.jdbi.v2.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Artist result set mapper.
 *
 * @author jtremeaux
 */
public class ArtistMapper extends BaseResultSetMapper<Artist> {
    public String[] getColumns() {
        return new String[] {
                "ART_ID_C",
                "ART_NAME_C",
                "ART_CREATEDATE_D",
                "ART_DELETEDATE_D"};
    }
    @Override
    public Artist map(int index, ResultSet r, StatementContext ctx) throws SQLException {
        final String[] columns = getColumns();
        int column = 0;
        return new Artist(
            r.getString(columns[column++]),
            r.getString(columns[column++]),
            r.getDate(columns[column++]),
            r.getDate(columns[column])
            );
    }
}
