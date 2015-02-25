package com.sismics.atmosphere.interceptor;

import org.atmosphere.cpr.Action;
import org.atmosphere.cpr.AtmosphereConfig;
import org.atmosphere.cpr.AtmosphereInterceptorAdapter;
import org.atmosphere.cpr.AtmosphereResource;
import org.skife.jdbi.v2.Handle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sismics.util.context.ThreadLocalContext;
import com.sismics.util.dbi.DBIF;

/**
 * This interceptor encapsulates each Atmosphere message into a DB transaction.
 *
 * @author jtremeaux
 */
public class DbiTransactionInterceptor extends AtmosphereInterceptorAdapter {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(DbiTransactionInterceptor.class);
    
    @Override
    public void configure(AtmosphereConfig config) {
        // NOP
    }

    @Override
    public Action inspect(AtmosphereResource r) {
        Handle handle = null;
        try {
            handle = DBIF.get().open();
        } catch (Exception e) {
            throw new RuntimeException("Cannot create DBI handle", e);
        }

        ThreadLocalContext context = ThreadLocalContext.get();
        context.setHandle(handle);
        handle.begin();

        return super.inspect(r);
    }

    @Override
    public void postInspect(AtmosphereResource r) {
        ThreadLocalContext context = ThreadLocalContext.get();
        Handle handle = context.getHandle();
        ThreadLocalContext.cleanup();
        
        if (handle.isInTransaction()) {
            try {
                handle.commit();
            } catch (Exception e) {
                log.error("Error during commit", e);
            }

            try {
                handle.close();
            } catch (Exception e) {
                log.error("Error closing JDBI handle", e);
            }
        }
    }
}
