package com.sismics.music.core.dao.dbi.criteria;

/**
 * User criteria.
 *
 * @author jtremeaux
 */
public class UserCriteria {
    /**
     * User is registered on Last.fm.
     */
    private boolean lastFmSessionTokenNotNull;

    public boolean isLastFmSessionTokenNotNull() {
        return this.lastFmSessionTokenNotNull;
    }

    public UserCriteria setLastFmSessionTokenNotNull(boolean lastFmSessionTokenNotNull) {
        this.lastFmSessionTokenNotNull = lastFmSessionTokenNotNull;
        return this;
    }
}
