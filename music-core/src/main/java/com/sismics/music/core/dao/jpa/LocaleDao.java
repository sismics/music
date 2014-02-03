package com.sismics.music.core.dao.jpa;

import com.sismics.music.core.dao.jpa.mapper.LocaleMapper;
import com.sismics.music.core.model.jpa.Locale;
import com.sismics.util.context.ThreadLocalContext;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.Query;

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
        try {
            return (Locale) handle.createQuery("select LOC_ID_C from T_LOCALE where id = :id").bind("id", id).map(new LocaleMapper()).first();
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Returns the list of all locales.
     * 
     * @return List of locales
     */
    @SuppressWarnings("unchecked")
    public List<Locale> findAll() {
        final Handle handle = ThreadLocalContext.get().getHandle();
        Query q = handle.createQuery("select LOC_ID_C from T_LOCALE order by LOC_ID_C asc");
        return q.map(new LocaleMapper()).list();
    }
}
