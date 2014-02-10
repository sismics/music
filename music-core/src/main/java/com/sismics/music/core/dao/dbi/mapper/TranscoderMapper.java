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
            "TRN_ID_C",
            "TRN_NAME_C",
            "TRN_SOURCE_C",
            "TRN_DESTINATION_C",
            "TRN_STEP1_C",
            "TRN_STEP2_C",
            "TRN_CREATEDATE_D",
            "TRN_DELETEDATE_D"
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
                r.getDate(columns[column++]),
                r.getDate(columns[column++])
                );
    }
}
