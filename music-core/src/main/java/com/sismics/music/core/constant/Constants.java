package com.sismics.music.core.constant;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

/**
 * Application constants.
 * 
 * @author jtremeaux
 */
public class Constants {
    /**
     * Default locale.
     */
    public static final String DEFAULT_LOCALE_ID = "en";

    /**
     * Default timezone ID.
     */
    public static final String DEFAULT_TIMEZONE_ID = "Europe/London";
    
    /**
     * Administrator's default password ("admin").
     */
    public static final String DEFAULT_ADMIN_PASSWORD = "$2a$05$6Ny3TjrW3aVAL1or2SlcR.fhuDgPKp5jp.P9fBXwVNePgeLqb4i3C";

    /**
     * Default generic user role.
     */
    public static final String DEFAULT_USER_ROLE = "user";
    
    /**
     * Supported audio file extensions.
     */
    public static final Set<String> SUPPORTED_AUDIO_EXTENSIONS = ImmutableSet.of(
            "ogg", "mp3", "flac", "mp4", "m4a", "m4p", "wma", "wav", "ra", "rm", "m4b");
}
