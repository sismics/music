package com.sismics.util.db;

import com.sismics.util.EnvironmentUtil;
import org.h2.tools.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

/**
 * Database console util.
 *
 * @author jtremeaux
 */
public class DbUtil {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(DbUtil.class);

    private static boolean started;

    private static Server h2Server;

    public static synchronized void start() {
        if (started || !EnvironmentUtil.isDevMode()) {
            return;
        }

        try {
            String serverOptions[] = new String[] { "-webPort", "8888" };
            h2Server = Server.createWebServer(serverOptions);
            h2Server.start();
            started = true;
        } catch (SQLException e) {
            log.error("Error starting H2 server", e);
        }
    }

    public static String getUrl() {
        return h2Server.getURL();
    }

    public static boolean isStarted() {
        return started;
    }
}
