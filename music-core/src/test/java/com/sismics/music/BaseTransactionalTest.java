package com.sismics.music;

import com.sismics.util.context.ThreadLocalContext;
import com.sismics.util.dbi.DBIF;
import org.junit.After;
import org.junit.Before;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

/**
 * Base class of tests with a transactional context.
 *
 * @author jtremeaux 
 */
public abstract class BaseTransactionalTest {
    @Before
    public void setUp() throws Exception {
        // Initialize the persistence layer
        DBI dbi = DBIF.get();
        Handle handle = dbi.open();
        ThreadLocalContext context = ThreadLocalContext.get();
        context.setHandle(handle);
        handle.begin();
    }

    @After
    public void tearDown() throws Exception {
    }
}
