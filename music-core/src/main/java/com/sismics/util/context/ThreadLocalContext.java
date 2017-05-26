package com.sismics.util.context;

import org.skife.jdbi.v2.Handle;

/**
 * Context associated to a user request, and stored in a ThreadLocal.
 * 
 * @author jtremeaux
 */
public class ThreadLocalContext {
    /**
     * ThreadLocal to store the context.
     */
    public static final ThreadLocal<ThreadLocalContext> threadLocalContext = new ThreadLocal<>();
    
    /**
     * JDBI handle.
     */
    private Handle handle;
    
    /**
     * Private constructor.
     */
    private ThreadLocalContext() {
        // NOP
    }
    
    /**
     * Returns an instance of this thread context.
     * 
     * @return Thread local context
     */
    public static ThreadLocalContext get() {
        ThreadLocalContext context = threadLocalContext.get();
        if (context == null) {
            context = new ThreadLocalContext();
            threadLocalContext.set(context);
        }
        return context;
    }

    /**
     * Cleans up the instance of this thread context.
     */
    public static void cleanup() {
        threadLocalContext.set(null);
    }
    
    /**
     * Getter of handle.
     *
     * @return handle
     */
    public Handle getHandle() {
        return handle;
    }

    /**
     * Setter of handle.
     *
     * @param handle handle
     */
    public void setHandle(Handle handle) {
        this.handle = handle;
    }
}
