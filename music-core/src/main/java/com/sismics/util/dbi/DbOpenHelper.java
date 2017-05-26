package com.sismics.util.dbi;

import com.google.common.base.Strings;
import com.google.common.io.CharStreams;
import com.sismics.music.core.util.ConfigUtil;
import com.sismics.util.ResourceUtil;
import org.skife.jdbi.v2.Handle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.*;

/**
 * A helper to update the database incrementally.
 *
 * @author jtremeaux
 */
public abstract class DbOpenHelper {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(DbOpenHelper.class);

    private final Handle handle;
    
    private final List<Exception> exceptions = new ArrayList<>();

    private boolean haltOnError;
    
    public DbOpenHelper(Handle handle) {
        this.handle = handle;
    }

    public void open() {
        log.info("Opening database and executing incremental updates");

        exceptions.clear();

        try {
            // Check if database is already created
            Integer oldVersion = null;
            try {
                List<Map<String,Object>> resultMap = handle.select("select c.value as ver from t_config c where c.id='DB_VERSION'");
                if (!resultMap.isEmpty()) {
                    String oldVersionStr = (String) resultMap.get(0).get("ver");
                    oldVersion = Integer.parseInt(oldVersionStr);
                }
            } catch (Exception e) {
                if (e.getMessage().contains("not found")) {
                    log.info("Unable to get database version: Table t_config not found");
                } else {
                    log.error("Unable to get database version", e);
                }
            }

            if (oldVersion == null) {
                // Execute creation script
                log.info("Executing initial schema creation script");
                onCreate();
                oldVersion = 0;
            }
            
            // Execute update script
            ResourceBundle configBundle = ConfigUtil.getConfigBundle();
            Integer currentVersion = Integer.parseInt(configBundle.getString("db.version"));
            log.info(MessageFormat.format("Found database version {0}, new version is {1}, executing database incremental update scripts", oldVersion, currentVersion));
            onUpgrade(oldVersion, currentVersion);
            log.info("Database upgrade complete");
        } catch (Exception e) {
            exceptions.add(e);
            log.error("Unable to complete schema update", e);
        }
    }

    /**
     * Execute all upgrade scripts in ascending order for a given version.
     * 
     * @param version Version number
     */
    protected void executeAllScript(final int version) throws Exception {
        List<String> fileNameList = ResourceUtil.list(getClass(), "/db/update/", (dir, name) -> {
            String versionString = String.format("%03d", version);
            return name.matches("dbupdate-" + versionString + "-\\d+\\.sql");
        });
        Collections.sort(fileNameList);
        
        for (String fileName : fileNameList) {
            if (log.isInfoEnabled()) {
                log.info(MessageFormat.format("Executing script: {0}", fileName));
            }
            InputStream is = getClass().getResourceAsStream("/db/update/" + fileName);
            executeScript(is);
        }
    }
    
    /**
     * Execute a SQL script.
     * 
     * @param inputScript Script to execute
     */
    private void executeScript(InputStream inputScript) throws Exception {
        String lines = CharStreams.toString(new InputStreamReader(inputScript));

        for (String sql : SQLSplitter.splitSQL(lines)) {
            if (Strings.isNullOrEmpty(sql) || sql.startsWith("--")) {
                continue;
            }
            
//            String formatted = formatter.format(sql);
            try {
                log.trace(sql);
                handle.update(sql);
            } catch (Exception e) {
                if (haltOnError) {
                    throw new RuntimeException("Error during script execution", e);
                }
                exceptions.add(e);
                if (log.isErrorEnabled()) {
                    log.error("Error executing SQL statement: {}", sql);
                    log.error(e.getMessage());
                }
            }
        }
    }

    public abstract void onCreate() throws Exception;
    
    public abstract void onUpgrade(int oldVersion, int newVersion) throws Exception;

    /**
     * Returns a List of all Exceptions which occured during the export.
     *
     * @return A List containig the Exceptions occured during the export
     */
    public List<?> getExceptions() {
        return exceptions;
    }

    public void setHaltOnError(boolean haltOnError) {
        this.haltOnError = haltOnError;
    }

    /**
     * Format the output SQL statements.
     *
     * @param format True to format
     */
    public void setFormat(boolean format) {
//        this.formatter = (format ? FormatStyle.DDL : FormatStyle.NONE).getFormatter();
    }
}
