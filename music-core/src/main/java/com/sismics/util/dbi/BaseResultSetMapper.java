package com.sismics.util.dbi;

import org.apache.commons.lang.StringUtils;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

/**
 * Base mapper.
 *
 * @author jtremeaux
 */
public abstract class BaseResultSetMapper<T> implements ResultSetMapper<T> {
    public abstract String[] getColumns();

    public String getJoinedColumns() {
        return StringUtils.join(getColumns(), ",");
    }

    public String getJoinedColumns(String prefix) {
        final String[] columns = getColumns();
        String[] prefixedColumns = new String[columns.length];
        for (int i = 0; i < getColumns().length; i++) {
            prefixedColumns[i] = prefix + "." + columns[i];
        }
        return StringUtils.join(prefixedColumns, ",");
    }
}
