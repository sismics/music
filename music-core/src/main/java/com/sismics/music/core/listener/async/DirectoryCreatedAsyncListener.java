package com.sismics.music.core.listener.async;

import com.google.common.base.Stopwatch;
import com.google.common.eventbus.Subscribe;
import com.sismics.music.core.event.async.DirectoryCreatedAsyncEvent;
import com.sismics.music.core.model.context.AppContext;
import com.sismics.music.core.model.dbi.Directory;
import com.sismics.music.core.service.collection.CollectionService;
import com.sismics.music.core.util.TransactionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;

/**
 * New directory created listener.
 *
 * @author jtremeaux
 */
public class DirectoryCreatedAsyncListener {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(DirectoryCreatedAsyncListener.class);

    /**
     * Process the event.
     *
     * @param directoryCreatedAsyncEvent New directory created event
     */
    @Subscribe
    public void onDirectoryCreated(final DirectoryCreatedAsyncEvent directoryCreatedAsyncEvent) throws Exception {
        if (log.isInfoEnabled()) {
            log.info("Directory created event: " + directoryCreatedAsyncEvent.toString());
        }
        Stopwatch stopwatch = Stopwatch.createStarted();

        final Directory directory = directoryCreatedAsyncEvent.getDirectory();

        TransactionUtil.handle(() -> {
            // Index new directory
            CollectionService collectionService = AppContext.getInstance().getCollectionService();
            collectionService.addDirectoryToIndex(directory);

            // Watch new directory
            AppContext.getInstance().getCollectionWatchService().watchDirectory(directory);

            // Update the scores
            collectionService.updateScore();
        });

        if (log.isInfoEnabled()) {
            log.info(MessageFormat.format("Collection updated completed in {0}", stopwatch));
        }
    }
}
