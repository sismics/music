package com.sismics.music.core.util;

import com.sismics.util.context.ThreadLocalContext;
import com.sismics.util.dbi.DBIF;
import org.skife.jdbi.v2.Handle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Database transaction utils.
 *
 * @author jtremeaux 
 */
public class TransactionUtil {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(TransactionUtil.class);

    /**
     * Encapsulate a process into a transactional context.
     * 
     * @param runnable Runnable
     */
    public static void handle(Runnable runnable) {
        Handle handle = ThreadLocalContext.get().getHandle();
        
        if (handle != null && handle.isInTransaction()) {
            // We are already in a transactional context, nothing to do
            runnable.run();
            return;
        }

        try {
            handle = DBIF.get().open();
        } catch (Exception e) {
            log.error("Cannot create DBI", e);
        }
        ThreadLocalContext context = ThreadLocalContext.get();
        context.setHandle(handle);
        handle.begin();
        
        try {
            runnable.run();
        } catch (Exception e) {
            ThreadLocalContext.cleanup();
            
            log.error("An exception occured, rolling back current transaction", e);

            // If an unprocessed error comes up, rollback the transaction
            if (handle.isInTransaction()) {
                handle.rollback();

                try {
                    handle.close();
                } catch (Exception ce) {
                    log.error("Error closing DBI handle", ce);
                }
            }
            return;
        }
        
        ThreadLocalContext.cleanup();

        // No error in the current request : commit the transaction
        if (handle.isInTransaction()) {
            if (handle.isInTransaction()) {
                handle.commit();
                
                try {
                    handle.close();
                } catch (Exception e) {
                    log.error("Error closing DBI handle", e);
                }
            }
        }
    }
    
    /**
     * Commits the current transaction, and begins a new one.
     */
    public static void commit() {
        Handle handle = ThreadLocalContext.get().getHandle();
        handle.commit();
        handle.begin();
    }
}
