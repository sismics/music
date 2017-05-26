package com.sismics.music.core.dao.dbi;

import com.sismics.music.core.model.dbi.Locale;
import com.sismics.util.context.ThreadLocalContext;
import org.skife.jdbi.v2.Handle;

import java.util.List;

/**
 * Locale DAO.
 * 
 * @author jtremeaux
 */
public class LocaleDao {
    /**
     * Gets a locale by its ID.
     * 
     * @param id Locale ID
     * @return Locale
     */
    public Locale getById(String id) {
        final Handle handle = ThreadLocalContext.get().getHandle();
        return handle.createQuery("select id from t_locale where id = :id")
                .bind("id", id)
                .mapTo(Locale.class)
                .first();
    }
    
    /**
     * Returns the list of all locales.
     * 
     * @return List of locales
     */
    public List<Locale> findAll() {
        final Handle handle = ThreadLocalContext.get().getHandle();
        return handle.createQuery("select id from t_locale order by id asc")
            .mapTo(Locale.class)
            .list();
    }
}
