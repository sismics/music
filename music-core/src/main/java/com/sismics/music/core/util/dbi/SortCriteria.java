package com.sismics.music.core.util.dbi;

/**
 * Sort criteria of a query.
 *
 * @author jtremeaux 
 */
public class SortCriteria {
    /**
     * Index of the column to sort (first is 0).
     */
    private int column;
    
    /**
     * Sort in increasing order (or else decreasing).
     */
    private boolean asc = true;

    private String sortQuery;

    /**
     * Constructor of sortCriteria.
     */
    public SortCriteria(String sortQuery) {
        this.sortQuery = sortQuery;
    }

    /**
     * Constructor of sortCriteria.
     */
    public SortCriteria(Integer column, Boolean asc) {
        if (column != null) {
            this.column = column;
        }
        if (asc != null) {
            this.asc = asc;
        }
    }

    public String getSortQuery() {
        return sortQuery;
    }

    /**
     * Getter of column.
     *
     * @return column
     */
    public int getColumn() {
        return column;
    }

    /**
     * Getter of asc.
     *
     * @return asc
     */
    public boolean isAsc() {
        return asc;
    }
}
