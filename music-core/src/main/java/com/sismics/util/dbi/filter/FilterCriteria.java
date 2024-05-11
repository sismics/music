package com.sismics.util.dbi.filter;

import java.util.ArrayList;
import java.util.List;

/**
 * Filtering criteria of a query.
 *
 * @author jtremeaux
 */
public class FilterCriteria {
    /**
     * Columns to sort.
     */
    private final List<FilterColumn> filterColumnList;

    /**
     * Constructor of SortCriteria.
     *
     * @param filterColumnList List of sort columns
     */
    public FilterCriteria(List<FilterColumn> filterColumnList) {
        this.filterColumnList = filterColumnList;
    }

    public List<FilterColumn> getFilterColumnList() {
        return filterColumnList;
    }
}