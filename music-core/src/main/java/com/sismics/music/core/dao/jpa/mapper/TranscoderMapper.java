package com.sismics.music.core.dao.jpa.mapper;

import com.sismics.music.core.model.jpa.Locale;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Album result set mapper.
 *
 * @author jtremeaux
 */
public class TranscoderMapper implements ResultSetMapper<Locale> {
    @Override
    public Locale map(int index, ResultSet r, StatementContext ctx) throws SQLException {
        return new Locale(r.getString("LOC_ID_C"));
    }
}
