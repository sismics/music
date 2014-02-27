package com.sismics.music.rest.util;

import java.text.MessageFormat;
import java.util.List;

import javax.servlet.ServletContext;

import org.apache.commons.lang.StringUtils;

import com.sismics.music.core.dao.dbi.LocaleDao;
import com.sismics.music.core.model.dbi.Locale;
import com.sismics.music.rest.dao.ThemeDao;
import com.sismics.rest.exception.ClientException;

/**
 * Utility class to validate parameters.
 *
 * @author jtremeaux
 */
public class ValidationUtil {
    /**
     * Validates a theme.
     *
     * @param servletContext Servlet context
     * @param themeId ID of the theme to validate
     * @param name Name of the parameter
     * @return String without white spaces
     * @param nullable True if the string can be empty or null
     * @throws com.sismics.rest.exception.ClientException
     */
    public static String validateTheme(ServletContext servletContext, String themeId, String name, boolean nullable) throws ClientException {
        themeId = StringUtils.strip(themeId);
        if (StringUtils.isEmpty(themeId)) {
            if (!nullable) {
                throw new ClientException("ValidationError", MessageFormat.format("{0} is required", name));
            } else {
                return null;
            }
        }
        ThemeDao themeDao = new ThemeDao();
        List<String> themeList = themeDao.findAll(servletContext);
        if (!themeList.contains(themeId)) {
            throw new ClientException("ValidationError", "Theme not found: " + themeId);
        }
        return themeId;
    }

    /**
     * Validates a locale.
     *
     * @param localeId String to validate
     * @param name Name of the parameter
     * @return String without white spaces
     * @param nullable True if the string can be empty or null
     * @throws ClientException
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
