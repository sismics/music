package com.sismics.atmosphere.interceptor;

import com.sismics.util.context.ThreadLocalContext;
import com.sismics.util.dbi.DBIF;
import org.atmosphere.cpr.Action;
import org.atmosphere.cpr.AtmosphereConfig;
import org.atmosphere.cpr.AtmosphereInterceptorAdapter;
import org.atmosphere.cpr.AtmosphereResource;
import org.skife.jdbi.v2.Handle;

/**
 * This interceptor encapsulates each Atmosphere message into a DB transaction.
 *
 * @author jtremeaux
 */
public class DbiTransactionInterceptor extends AtmosphereInterceptorAdapter {
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
        ThreadLocalContext.cleanup();

        // No error processing the request : commit / rollback the current transaction depending on the HTTP code
//        if (handle.isInTransaction()) {
//            HttpServletResponse r = (HttpServletResponse) response;
//            int statusClass = r.getStatus() / 100;
//            if (statusClass == 2 || statusClass == 3) {
//                try {
//                    handle.commit();
//                } catch (Exception e) {
//                    log.error("Error during commit", e);
//                    r.sendError(500);
//                }
//            } else {
//                handle.rollback();
//            }
//
//            try {
//                handle.close();
//            } catch (Exception e) {
//                log.error("Error closing JDBI handle", e);
//            }
//        }
//        } catch (Exception e) {
//            ThreadLocalContext.cleanup();
//
//            log.error("An exception occured, rolling back current transaction", e);
//
//            // If an unprocessed error comes up from the application layers (Jersey...), rollback the transaction
//            if (handle.isInTransaction()) {
//                handle.rollback();
//
//                try {
//                    handle.close();
//                } catch (Exception ce) {
//                    log.error("Error closing DBI handle", ce);
//                }
//            }
//            throw new RuntimeException(e);
//        }
    }
}
