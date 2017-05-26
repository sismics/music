package com.sismics.music.core.dao.dbi.mapper;

import com.sismics.music.core.model.dbi.Transcoder;
import com.sismics.util.dbi.BaseResultSetMapper;
import org.skife.jdbi.v2.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Album result set mapper.
 *
 * @author jtremeaux
 */
public class TranscoderMapper extends BaseResultSetMapper<Transcoder> {
    public String[] getColumns() {
        return new String[] {
            "id",
            "name",
            "source",
            "destination",
            "step1",
            "step2",
            "createdate",
            "deletedate"
        };
    }

    @Override
    public Transcoder map(int index, ResultSet r, StatementContext ctx) throws SQLException {
        final String[] columns = getColumns();
        int column = 0;
        return new Transcoder(
                r.getString(columns[column++]),
                r.getString(columns[column++]),
                r.getString(columns[column++]),
                r.getString(columns[column++]),
                r.getString(columns[column++]),
                r.getString(columns[column++]),
                r.getTimestamp(columns[column++]),
                r.getTimestamp(columns[column++])
                );
    }
}
