package com.sismics.music.core.model.context;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.sismics.music.core.listener.async.CollectionReindexAsyncListener;
import com.sismics.music.core.listener.async.DirectoryCreatedAsyncListener;
import com.sismics.music.core.listener.async.DirectoryDeletedAsyncListener;
import com.sismics.music.core.listener.sync.DeadEventListener;
import com.sismics.music.core.service.CollectionService;
import com.sismics.util.EnvironmentUtil;

/**
 * Global application context.
 *
 * @author jtremeaux 
 */
public class AppContext {
    /**
     * Singleton instance.
     */
    private static AppContext instance;

    /**
     * Event bus.
     */
    private EventBus eventBus;
    
    /**
     * Generic asynchronous event bus.
     */
    private EventBus asyncEventBus;

    /**
     * Collection indexing asynchronous event bus.
     */
    private EventBus collectionEventBus;

    /**
     * Collection service.
     */
    private CollectionService collectionService;

    /**
     * Asynchronous executors.
     */
    private List<ExecutorService> asyncExecutorList;
    
    /**
     * Private constructor.
     */
    private AppContext() {
        resetEventBus();

        collectionService = new CollectionService();
        collectionService.startAndWait();

    }
    
    /**
     * (Re)-initializes the event buses.
     */
    private void resetEventBus() {
        eventBus = new EventBus();
        eventBus.register(new DeadEventListener());
        
        asyncExecutorList = new ArrayList<ExecutorService>();
        
        asyncEventBus = newAsyncEventBus();
        collectionEventBus = newAsyncEventBus();
        collectionEventBus.register(new DirectoryCreatedAsyncListener());
        collectionEventBus.register(new DirectoryDeletedAsyncListener());
        collectionEventBus.register(new CollectionReindexAsyncListener());
    }

    /**
     * Returns a single instance of the application context.
     * 
     * @return Application context
     */
    public static AppContext getInstance() {
        if (instance == null) {
            instance = new AppContext();
        }
        return instance;
    }
    
    /**
     * Creates a new asynchronous event bus.
     * 
     * @return Async event bus
     */
    private EventBus newAsyncEventBus() {
        if (EnvironmentUtil.isUnitTest()) {
            return new EventBus();
        } else {
            ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1,
                    0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>());
            asyncExecutorList.add(executor);
            return new AsyncEventBus(executor);
        }
    }

    /**
     * Getter of eventBus.
     *
     * @return eventBus
     */
    public EventBus getEventBus() {
        return eventBus;
    }

    /**
     * Getter of collectionService.
     *
     * @return collectionService
     */
    public CollectionService getCollectionService() {
        return collectionService;
    }

    /**
     * Getter of asyncEventBus.
     *
     * @return asyncEventBus
     */
    public EventBus getAsyncEventBus() {
        return asyncEventBus;
    }

    /**
     * Getter of collectionEventBus.
     *
     * @return collectionEventBus
     */
    public EventBus getCollectionEventBus() {
        return collectionEventBus;
    }
}
