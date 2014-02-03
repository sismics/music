package com.sismics.music.core.dao.jpa.mapper;

import com.sismics.music.core.model.jpa.BaseFunction;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Basefunction result set mapper.
 *
 * @author jtremeaux
 */
public class BaseFunctionMapper implements ResultSetMapper<BaseFunction> {
    @Override
    public BaseFunction map(int index, ResultSet r, StatementContext ctx) throws SQLException {
        return new BaseFunction(r.getString("BAF_ID_C"));
    }
}
