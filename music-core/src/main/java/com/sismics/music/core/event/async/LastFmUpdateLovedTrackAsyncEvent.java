package com.sismics.music.core.event.async;

import com.google.common.base.Objects;
import com.sismics.music.core.model.dbi.User;

/**
 * Last.fm update loved tracks event.
 *
 * @author jtremeaux
 */
public class LastFmUpdateLovedTrackAsyncEvent {
    /**
     * User.
     */
    private User user;

    public LastFmUpdateLovedTrackAsyncEvent(User user) {
        this.user = user;
    }

    /**
     * Getter of user.
     *
     * @return user
     */
    public User getUser() {
        return user;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("user", user)
                .toString();
    }
}
