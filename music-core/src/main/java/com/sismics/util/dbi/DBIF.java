package com.sismics.util.dbi;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.sismics.music.core.dao.dbi.mapper.*;
import com.sismics.music.core.util.DirectoryUtil;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * DBI factory.
 *
 * @author jtremeaux
 */
public class DBIF {
    private static final Logger log = LoggerFactory.getLogger(DBIF.class);

    private static ComboPooledDataSource cpds;

    private static DBI dbi;

    static {
        if (dbi == null) {
            createDbi();
        }
    }

    public static void createDbi() {
        try {
            cpds = new ComboPooledDataSource(); // TODO use getEntityManagerProperties()
            dbi = new DBI(cpds);
            dbi.registerMapper(new AlbumMapper());
            dbi.registerMapper(new ArtistMapper());
            dbi.registerMapper(new AuthenticationTokenMapper());
            dbi.registerMapper(new BaseFunctionMapper());
            dbi.registerMapper(new ConfigMapper());
            dbi.registerMapper(new DirectoryMapper());
            dbi.registerMapper(new LocaleMapper());
            dbi.registerMapper(new PlaylistMapper());
            dbi.registerMapper(new PlaylistTrackMapper());
            dbi.registerMapper(new RoleBaseFunctionMapper());
            dbi.registerMapper(new RoleMapper());
            dbi.registerMapper(new TrackMapper());
            dbi.registerMapper(new TranscoderMapper());
            dbi.registerMapper(new UserMapper());
        } catch (Throwable t) {
            log.error("Error creating DBI", t);
        }

        Handle handle = null;
        try {
            handle = dbi.open();
            DbOpenHelper openHelper = new DbOpenHelper(handle) {

                @Override
                public void onCreate() throws Exception {
                    executeAllScript(0);
                }

                @Override
                public void onUpgrade(int oldVersion, int newVersion) throws Exception {
                    for (int version = oldVersion + 1; version <= newVersion; version++) {
                        executeAllScript(version);
                    }
                }
            };
            openHelper.open();
        } catch (Exception e) {
            if (handle != null && handle.isInTransaction()) {
                handle.rollback();
                handle.close();
            }
        }

    }

    private static Map<Object, Object> getEntityManagerProperties() {
        // Use properties file if exists
        try {
            URL hibernatePropertiesUrl = DBIF.class.getResource("/c3p0.properties");
            if (hibernatePropertiesUrl != null) {
                log.info("Configuring connection pool from c3p0.properties");

                InputStream is = hibernatePropertiesUrl.openStream();
                Properties properties = new Properties();
                properties.load(is);
                return properties;
            }
        } catch (IOException e) {
            log.error("Error reading c3p0.properties", e);
        }

        // Use environment parameters
        log.info("Configuring EntityManager from environment parameters");
        Map<Object, Object> props = new HashMap<Object, Object>();
        props.put("c3p0.driverClass", "org.hsqldb.jdbcDriver");
        File dbDirectory = DirectoryUtil.getDbDirectory();
        String dbFile = dbDirectory.getAbsoluteFile() + File.separator + "music";
        props.put("c3p0.jdbcUrl", "jdbc:hsqldb:file:" + dbFile + ";hsqldb.write_delay=false;shutdown=true");
        props.put("c3p0.user", "sa");
        return props;
    }

    /**
     * Private constructor.
     */
    private DBIF() {
    }

    /**
     * Returns an instance of DBIF.
     *
     * @return Instance of DBIF
     */
    public static DBI get() {
        return dbi;
    }

    public static void reset() {
        if (cpds != null) {
            dbi.open().createStatement("SHUTDOWN").execute();
            cpds.close();
            cpds = null;
            dbi = null;
            createDbi();
        }
    }
}
