package com.sismics.music.core.event;

import com.google.common.base.Objects;
import com.sismics.music.core.model.jpa.Track;

/**
 * Event raised on request to update a feed favicon.
 *
 * @author jtremeaux 
 */
public class FaviconUpdateRequestedEvent {
    /**
     * Feed to update.
     */
    private Track feed;
    
    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("feedId", feed.getId())
                .toString();
    }

    /**
     * Getter of feed.
     *
     * @return feed
     */
    public Track getFeed() {
        return feed;
    }

    /**
     * Setter of feed.
     *
     * @param feed feed
     */
    public void setFeed(Track feed) {
        this.feed = feed;
    }
}
