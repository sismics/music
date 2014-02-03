package com.sismics.music.core.dao.jpa;

import com.sismics.music.core.model.jpa.Locale;
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
        return handle.createQuery("select LOC_ID_C from T_LOCALE where LOC_ID_C = :id")
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
        return handle.createQuery("select LOC_ID_C from T_LOCALE order by LOC_ID_C asc")
            .mapTo(Locale.class)
            .list();
    }
}
