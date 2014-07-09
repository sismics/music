package com.sismics.music.core.util.dbi;

import java.util.Map;
import java.util.Map.Entry;

import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.Query;

import com.sismics.util.context.ThreadLocalContext;

/**
 * Query utilities.
 *
 * @author jtremeaux 
 */
public class QueryUtil {
    /**
     * Creates a native query from the query parameters.
     * 
     * @param queryParam Query parameters
     * @return Native query
     */
    public static Query<Map<String, Object>> getNativeQuery(QueryParam queryParam) {
        return getNativeQuery(queryParam, 0, 0);
    }
    
    /**
     * Creates a native query from the query parameters.
     * 
     * @param queryParam Query parameters
     * @param limit Limit
     * @param offset Offset
     * @return Native query
     */
    public static Query<Map<String, Object>> getNativeQuery(QueryParam queryParam, int limit, int offset) {
        final Handle handle = ThreadLocalContext.get().getHandle();
        Query<Map<String, Object>> query = handle.createQuery(queryParam.getQueryString()
                + (limit > 0 ? " LIMIT " + limit : "")
                + (offset > 0 ? " OFFSET " + offset : ""));
        for (Entry<String, Object> entry : queryParam.getParameterMap().entrySet()) {
            query.bind(entry.getKey(), entry.getValue());
        }
        return query;
    }
}
