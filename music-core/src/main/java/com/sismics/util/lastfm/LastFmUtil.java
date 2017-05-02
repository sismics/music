package com.sismics.util.lastfm;

import de.umass.lastfm.*;

/**
 * Last.fm utilities.
 *
 * @author jtremeaux
 */
public class LastFmUtil {
    /**
     * Retrieves the loved tracks by a user.
     *
     * @param user The user name to fetch the tracks for
     * @param page The page number to scan to
     * @param limit Limit (default 1000)
     * @param apiKey A Last.fm API key.
     * @return The loved tracks
     */
    public static PaginatedResult<Track> getLovedTracks(String user, int page, int limit, String apiKey) {
        Result result = Caller.getInstance().call("user.getLovedTracks", apiKey, "user", user, "page", String.valueOf(page), "limit", String.valueOf(limit));
        return ResponseBuilder.buildPaginatedResult(result, Track.class);
    }

    /**
     * Retrieves the tracks from the user library.
     *
     * @param user The user name to fetch the tracks for
     * @param page The page number to scan to
     * @param limit Limit (default 1000)
     * @param apiKey A Last.fm API key.
     * @return The tracks
     */
    public static PaginatedResult<Track> getAllTracks(String user, int page, int limit, String apiKey) {
        Result result = Caller.getInstance().call("library.getTracks", apiKey, "user", user, "page", String.valueOf(page), "limit", String.valueOf(limit));
        return ResponseBuilder.buildPaginatedResult(result, Track.class);
    }
}
