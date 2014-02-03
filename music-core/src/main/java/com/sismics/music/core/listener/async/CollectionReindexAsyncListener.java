package com.sismics.music.core.listener.async;

import com.google.common.eventbus.Subscribe;
import com.sismics.music.core.event.async.CollectionReindexAsyncEvent;
import com.sismics.music.core.model.context.AppContext;
import com.sismics.music.core.model.jpa.Directory;
import com.sismics.music.core.service.CollectionService;
import com.sismics.music.core.util.TransactionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;

/**
 * Collection reindex listener.
 *
 * @author jtremeaux
 */
public class CollectionReindexAsyncListener {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(CollectionReindexAsyncListener.class);

    /**
     * Process the event.
     *
     * @param collectionReindexAsyncEvent Collection reindex event
     * @throws Exception
     */
    @Subscribe
    public void onCollectionReindex(final CollectionReindexAsyncEvent collectionReindexAsyncEvent) throws Exception {
        if (log.isInfoEnabled()) {
            log.info("Collection reindex event: " + collectionReindexAsyncEvent.toString());
        }
        long startTime = System.currentTimeMillis();

        TransactionUtil.handle(new Runnable() {
            @Override
            public void run() {
                // Index new directory
                CollectionService collectionService = AppContext.getInstance().getCollectionService();
                collectionService.reindex();
            }
        });

        long endTime = System.currentTimeMillis();
        if (log.isInfoEnabled()) {
            log.info(MessageFormat.format("Collection updated in {0}ms", endTime - startTime));
        }
    }
}
