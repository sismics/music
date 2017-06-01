package com.sismics.music.core.util.dbi;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.sismics.util.context.ThreadLocalContext;
import com.sismics.util.dbi.filter.FilterColumn;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.Query;
import org.skife.jdbi.v2.util.IntegerMapper;

import java.util.List;
import java.util.Map;

/**
 * Utilities for paginated lists.
 * 
 * @author jtremeaux
 */
public class PaginatedLists {
    /**
     * Default size of a page.
     */
    private static final int DEFAULT_PAGE_SIZE = 10;

    /**
     * Maximum size of a page.
     */
    private static final int MAX_PAGE_SIZE = 99999; // FIXME paginate the Android app

    /**
     * Constructs a paginated list.
     * 
     * @param pageSize Size of the page
     * @param offset Offset of the page
     * @return Paginated list
     */
    public static <E> PaginatedList<E> create(Integer pageSize, Integer offset) {
        if (pageSize == null) {
            pageSize = DEFAULT_PAGE_SIZE;
        }
        if (offset == null) {
            offset = 0;
        }
        if (pageSize > MAX_PAGE_SIZE) {
            pageSize = MAX_PAGE_SIZE;
        }
        if (pageSize == 0) {
            pageSize = 1; // Page size of zero counterintuitively returns all rows, we don't want to kill the database
        }
        return new PaginatedList<>(pageSize, offset);
    }
    
    /**
     * Constructs a paginated list with default parameters.
     * 
     * @return Paginated list
     */
    public static <E> PaginatedList<E> create() {
        return create(null, null);
    }

    /**
     * Executes a non paginated query.
     *
     * @param queryParam Query parameters
     */
    @SuppressWarnings("unchecked")
    public static <E> List<E> executeQuery(QueryParam queryParam) {
        StringBuilder sb = new StringBuilder(getQueryString(queryParam));
        if (queryParam.getSortCriteria() != null) {
            sb.append(getOrderByClause(queryParam.getSortCriteria()));
        }

        final Handle handle = ThreadLocalContext.get().getHandle();
        Query query = handle.createQuery(sb.toString());
        mapQueryParam(query, queryParam);
        mapFilterColumn(query, queryParam);

        if (queryParam.getResultMapper() != null) {
            return query.map(queryParam.getResultMapper()).list();
        } else {
            return query.list();
        }
    }

    /**
     * Executes a native count(*) request to count the number of results.
     * 
     * @param paginatedList Paginated list object containing parameters, and into which results are added by side effects
     * @param queryParam Query parameters
     */
    public static <E> void executeCountQuery(PaginatedList<E> paginatedList, QueryParam queryParam) {
        final Handle handle = ThreadLocalContext.get().getHandle();
        Query query = handle.createQuery(getNativeCountQuery(queryParam));

        mapQueryParam(query, queryParam);
        mapFilterColumn(query, queryParam);
        Integer resultCount = (Integer) query.map(IntegerMapper.FIRST).first();
        paginatedList.setResultCount(resultCount);
    }

    /**
     * Returns the native query to count the number of records.
     * The initial query must be of the form "select xx from yy".
     *
     * @param queryParam Query parameters
     * @return Count query
     */
    private static String getNativeCountQuery(QueryParam queryParam) {
        return "select count(*) as result_count from (" +
                getQueryString(queryParam) +
                ") as t1";
    }

    /**
     * Executes a query and returns the data of the current page.
     *
     * @param paginatedList Paginated list object containing parameters, and into which results are added by side effects
     * @param queryParam Query parameters
     */
    @SuppressWarnings("unchecked")
    private static <E> void executeResultQuery(PaginatedList<E> paginatedList, QueryParam queryParam) {
        final Handle handle = ThreadLocalContext.get().getHandle();
        StringBuilder sb = new StringBuilder(getQueryString(queryParam));
        if (queryParam.getSortCriteria() != null) {
            sb.append(getOrderByClause(queryParam.getSortCriteria()));
        }
        Query query = handle.createQuery(getLimitQuery(sb.toString(), paginatedList.getLimit(), paginatedList.getOffset()));

        mapQueryParam(query, queryParam);
        mapFilterColumn(query, queryParam);

        if (queryParam.getResultMapper() != null) {
            paginatedList.setResultList(query.map(queryParam.getResultMapper()).list());
        } else {
            paginatedList.setResultList(query.list());
        }
    }

    /**
     * Creates a limit query.
     *
     * @param queryString Query parameters
     * @param limit Limit
     * @param offset Offset
     * @return Native query
     */
    public static String getLimitQuery(String queryString, int limit, int offset) {
        return queryString
                + (limit > 0 ? " LIMIT " + limit : "")
                + (offset > 0 ? " OFFSET " + offset : "");
    }

    /**
     * Executes a paginated request with 2 native queries (one to count the number of results, and one to return the page).
     *
     * @param paginatedList Paginated list object containing parameters, and into which results are added by side effects
     * @param queryParam Query parameters
     * @param sortCriteria Sort criteria
     */
    public static <E> void executePaginatedQuery(PaginatedList<E> paginatedList, QueryParam queryParam, SortCriteria sortCriteria) {
        if (sortCriteria != null) {
            queryParam.setSortCriteria(sortCriteria);
        }
        executeCountQuery(paginatedList, queryParam);
        executeResultQuery(paginatedList, queryParam);
    }

    private static String getQueryString(QueryParam queryParam) {
        StringBuilder sb = new StringBuilder(queryParam.getQueryString());

        List<String> whereList = Lists.newLinkedList(queryParam.getCriteriaList());
        if (queryParam.getFilterCriteria() != null && !queryParam.getFilterCriteria().getFilterColumnList().isEmpty()) {
            for (FilterColumn filterColumn : queryParam.getFilterCriteria().getFilterColumnList()) {
                whereList.add(filterColumn.getPredicate());
            }
        }
        if (!whereList.isEmpty()) {
            sb.append(" where ");
            sb.append(Joiner.on(" and ").join(whereList));
        }
        if (queryParam.getGroupByList() != null && !queryParam.getGroupByList().isEmpty()) {
            sb.append(" group by ");
            sb.append(Joiner.on(", ").join(queryParam.getGroupByList()));
        }

        return sb.toString();
    }

    /**
     * Get the order by clause from the sort criteria.
     *
     * @param sortCriteria Sort criteria
     * @return Order by clause
     */
    private static String getOrderByClause(SortCriteria sortCriteria) {
        String sortQuery = sortCriteria.getSortQuery();
        if (sortQuery != null) {
            return sortQuery;
        } else {
            return " order by c" +
                    sortCriteria.getColumn() +
                    (sortCriteria.isAsc() ? " asc" : " desc");
        }
    }

    private static void mapQueryParam(Query query, QueryParam queryParam) {
        for (Map.Entry<String, Object> parameter : queryParam.getParameterMap().entrySet()) {
            query.bind(parameter.getKey(), parameter.getValue());
        }
    }

    private static void mapFilterColumn(Query query, QueryParam queryParam) {
        if (queryParam.getFilterCriteria() != null) {
            for (FilterColumn filterColumn : queryParam.getFilterCriteria().getFilterColumnList()) {
                if (filterColumn.hasParam()) {
                    query.bind(filterColumn.getParamName(), filterColumn.getParamValue());
                }
            }
        }
    }
}
