package com.sismics.music.core.dao.dbi.mapper;

import com.sismics.music.core.model.dbi.Locale;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Locale result set mapper.
 *
 * @author jtremeaux
 */
public class LocaleMapper implements ResultSetMapper<Locale> {
    @Override
    public Locale map(int index, ResultSet r, StatementContext ctx) throws SQLException {
        return new Locale(r.getString("id"));
    }
}
