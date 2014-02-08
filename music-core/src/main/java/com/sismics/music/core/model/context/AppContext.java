package com.sismics.music.core.model.context;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.sismics.music.core.listener.async.*;
import com.sismics.music.core.listener.sync.DeadEventListener;
import com.sismics.music.core.service.albumart.AlbumArtService;
import com.sismics.music.core.service.collection.CollectionService;
import com.sismics.music.core.service.player.PlayerService;
import com.sismics.util.EnvironmentUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
     * Scrobbler asynchronous event bus.
     */
    private EventBus scrobblerEventBus;

    /**
     * Collection service.
     */
    private CollectionService collectionService;

    /**
     * Album art service.
     */
    private AlbumArtService albumArtService;

    /**
     * Player service.
     */
    private PlayerService playerService;

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

        albumArtService = new AlbumArtService();
        playerService = new PlayerService();
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

        scrobblerEventBus = newAsyncEventBus();
        scrobblerEventBus.register(new PlayStartedAsyncListener());
        scrobblerEventBus.register(new PlayCompletedAsyncListener());
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
     * Getter of albumArtService.
     *
     * @return albumArtService
     */
    public AlbumArtService getAlbumArtService() {
        return albumArtService;
    }

    /**
     * Getter of playerService.
     *
     * @return playerService
     */
    public PlayerService getPlayerService() {
        return playerService;
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

    /**
     * Getter of scrobblerEventBus.
     *
     * @return scrobblerEventBus
     */
    public EventBus getScrobblerEventBus() {
        return scrobblerEventBus;
    }
}
