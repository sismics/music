package com.sismics.music.core.util.dbi;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.exceptions.ResultSetException;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * Maps the results set to an indexed array.
 *
 * @author jtremeaux
 */
public class ColumnIndexMapper implements ResultSetMapper<Object[]> {
    /**
     * An instance of ColumnIndexMapper.
     */
    public static final ColumnIndexMapper INSTANCE = new ColumnIndexMapper();

    public Object[] map(int index, ResultSet r, StatementContext ctx) {
        ResultSetMetaData m;
        try {
            m = r.getMetaData();
        } catch (SQLException e) {
            throw new ResultSetException("Unable to obtain metadata from result set", e, ctx);
        }

        Object[] row = null;
        try {
            row = new Object[m.getColumnCount()];
            for (int i = 1; i <= m.getColumnCount(); i++) {
                row[i - 1] = r.getObject(i);
            }
        } catch (SQLException e) {
            throw new ResultSetException("Unable to access specific metadata from " +
                    "result set metadata", e, ctx);
        }
        return row;
    }
}
