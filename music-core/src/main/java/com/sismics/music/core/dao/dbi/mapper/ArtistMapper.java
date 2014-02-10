package com.sismics.music.core.dao.dbi.mapper;

import com.sismics.music.core.model.dbi.Artist;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Artist result set mapper.
 *
 * @author jtremeaux
 */
public class ArtistMapper implements ResultSetMapper<Artist> {
    @Override
    public Artist map(int index, ResultSet r, StatementContext ctx) throws SQLException {
        return new Artist(
                r.getString("ART_ID_C"),
                r.getString("ART_NAME_C"),
                r.getDate("ART_CREATEDATE_D"),
                r.getDate("ART_DELETEDATE_D")
                );
    }
}
