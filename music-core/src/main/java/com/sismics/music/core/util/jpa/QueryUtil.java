package com.sismics.music.core.util.jpa;

import com.sismics.util.context.ThreadLocalContext;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.Query;

import java.util.Map.Entry;

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
    public static Query getNativeQuery(QueryParam queryParam) {
        final Handle handle = ThreadLocalContext.get().getHandle();
        Query query = handle.createQuery(queryParam.getQueryString());
        for (Entry<String, Object> entry : queryParam.getParameterMap().entrySet()) {
            query.bind(entry.getKey(), entry.getValue());
        }
        return query;
    }
}
