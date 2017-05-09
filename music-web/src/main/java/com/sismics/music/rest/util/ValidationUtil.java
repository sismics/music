package com.sismics.music.rest.util;

import com.sismics.music.core.dao.dbi.LocaleDao;
import com.sismics.music.core.model.dbi.Locale;
import com.sismics.rest.exception.ClientException;
import org.apache.commons.lang.StringUtils;

import java.text.MessageFormat;

/**
 * Utility class to validate parameters.
 *
 * @author jtremeaux
 */
public class ValidationUtil {
    /**
     * Validates a locale.
     *
     * @param localeId String to validate
     * @param name Name of the parameter
     * @return String without white spaces
     * @param nullable True if the string can be empty or null
     */
    public static String validateLocale(String localeId, String name, boolean nullable) throws ClientException {
        localeId = StringUtils.strip(localeId);
        if (StringUtils.isEmpty(localeId)) {
            if (!nullable) {
                throw new ClientException("ValidationError", MessageFormat.format("{0} is required", name));
            } else {
                return null;
            }
        }
        LocaleDao localeDao = new LocaleDao();
        Locale locale = localeDao.getById(localeId);
        if (locale == null) {
            throw new ClientException("ValidationError", "Locale not found: " + localeId);
        }
        return localeId;
    }
}
