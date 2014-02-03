package com.sismics.music.core.event.async;

import com.google.common.base.Objects;
import com.sismics.music.core.model.jpa.Directory;

/**
 * Collection reindex event.
 *
 * @author jtremeaux
 */
public class CollectionReindexAsyncEvent {

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .toString();
    }
}
