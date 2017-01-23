package com.sismics.music.core.util.dbi;

import com.sismics.util.dbi.filter.FilterCriteria;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.util.List;
import java.util.Map;

/**
 * Query parameters.
 *
 * @author jtremeaux 
 */
public class QueryParam {

    /**
     * Query string.
     */
    private String queryString;

    /**
     * Query criteria.
     */
    private List<String> criteriaList;

    /**
     * Sort criteria.
     */
    private SortCriteria sortCriteria;

    /**
     * Query parameters.
     */
    private Map<String, Object> parameterMap;

    /**
     * Filter criteria.
     */
    private FilterCriteria filterCriteria;

    /**
     * Group by criteria.
     */
    private List<String> groupByList;

    /**
     * Result mapper for native queries.
     */
    private ResultSetMapper resultMapper;

    /**
     * Constructor of QueryParam.
     *
     * @param queryString Query string
     * @param parameterMap Query parameters
     * @param sortCriteria Sort criteria
     * @param groupByList Group by criteria
     */
    public QueryParam(String queryString, List<String> criteriaList, Map<String, Object> parameterMap, SortCriteria sortCriteria, FilterCriteria filterCriteria, List<String> groupByList,
                      ResultSetMapper resultMapper) {
        this.queryString = queryString;
        this.criteriaList = criteriaList;
        this.parameterMap = parameterMap;
        this.sortCriteria = sortCriteria;
        this.filterCriteria = filterCriteria;
        this.groupByList = groupByList;
        this.resultMapper = resultMapper;
    }

    /**
     * Constructor of QueryParam.
     *
     * @param queryString Query string
     * @param parameterMap Query parameters
     * @param sortCriteria Sort criteria
     */
    public QueryParam(String queryString, List<String> criteriaList, Map<String, Object> parameterMap, SortCriteria sortCriteria, FilterCriteria filterCriteria,
                      ResultSetMapper resultMapper) {
        this(queryString, criteriaList, parameterMap, sortCriteria, filterCriteria, null, resultMapper);
    }

    public String getQueryString() {
        return queryString;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public SortCriteria getSortCriteria() {
        return sortCriteria;
    }

    public List<String> getCriteriaList() {
        return criteriaList;
    }

    public Map<String, Object> getParameterMap() {
        return parameterMap;
    }

    public FilterCriteria getFilterCriteria() {
        return filterCriteria;
    }

    public List<String> getGroupByList() {
        return groupByList;
    }

    public ResultSetMapper getResultMapper() {
        return resultMapper;
    }

    public void setSortCriteria(SortCriteria sortCriteria) {
        this.sortCriteria = sortCriteria;
    }
}
