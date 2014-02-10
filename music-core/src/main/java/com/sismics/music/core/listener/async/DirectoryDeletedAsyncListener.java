package com.sismics.music.core.listener.async;

import com.google.common.eventbus.Subscribe;
import com.sismics.music.core.event.async.DirectoryDeletedAsyncEvent;
import com.sismics.music.core.model.context.AppContext;
import com.sismics.music.core.model.dbi.Directory;
import com.sismics.music.core.service.collection.CollectionService;
import com.sismics.music.core.util.TransactionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;

/**
 * Directory deleted listener.
 *
 * @author jtremeaux
 */
public class DirectoryDeletedAsyncListener {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(DirectoryDeletedAsyncListener.class);

    /**
     * Process the event.
     *
     * @param directoryDeletedAsyncEvent New directory deleted event
     * @throws Exception
     */
    @Subscribe
    public void onDirectoryDeleted(final DirectoryDeletedAsyncEvent directoryDeletedAsyncEvent) throws Exception {
        if (log.isInfoEnabled()) {
            log.info("Directory deleted event: " + directoryDeletedAsyncEvent.toString());
        }
        long startTime = System.currentTimeMillis();

        final Directory directory = directoryDeletedAsyncEvent.getDirectory();

        TransactionUtil.handle(new Runnable() {
            @Override
            public void run() {
                // Remove directory from index
                CollectionService collectionService = AppContext.getInstance().getCollectionService();
                collectionService.removeDirectoryFromIndex(directory);
            }
        });

        long endTime = System.currentTimeMillis();
        if (log.isInfoEnabled()) {
            log.info(MessageFormat.format("Collection updated in {0}ms", endTime - startTime));
        }
    }
}
