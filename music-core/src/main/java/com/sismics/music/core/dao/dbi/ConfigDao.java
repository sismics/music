package com.sismics.music.core.dao.dbi;

import com.sismics.music.core.constant.ConfigType;
import com.sismics.music.core.model.dbi.Config;
import com.sismics.util.context.ThreadLocalContext;
import org.skife.jdbi.v2.Handle;

/**
 * Configuration parameter DAO.
 * 
 * @author jtremeaux
 */
public class ConfigDao {
    /**
     * Gets a configuration parameter by its ID.
     * 
     * @param id Configuration parameter ID
     * @return Configuration parameter
     */
    public Config getById(ConfigType id) {
        final Handle handle = ThreadLocalContext.get().getHandle();

        // Prevents from getting parameters outside of a transactional context (e.g. jUnit)
        if (handle == null) {
            return null;
        }
        return handle.createQuery("select id, value" +
                "  from t_config" +
                "  where id = :id")
                .bind("id", id.name())
                .mapTo(Config.class)
                .first();
    }
}
