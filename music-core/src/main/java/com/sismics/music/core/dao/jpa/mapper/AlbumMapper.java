package com.sismics.music.core.dao.jpa.mapper;

import com.sismics.music.core.model.jpa.Album;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Album result set mapper.
 *
 * @author jtremeaux
 */
public class AlbumMapper implements ResultSetMapper<Album> {
    @Override
    public Album map(int index, ResultSet r, StatementContext ctx) throws SQLException {
        return new Album(
                r.getString("ALB_ID_C"),
                r.getString("ALB_IDDIRECTORY_C"),
                r.getString("ALB_IDARTIST_C"),
                r.getString("ALB_NAME_C"),
                r.getString("ALB_ALBUMART_C"),
                r.getDate("ALB_CREATEDATE_D"),
                r.getDate("ALB_DELETEDATE_D"));
    }
}
