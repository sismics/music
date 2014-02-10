package com.sismics.music.core.event;

import com.google.common.base.Objects;
import com.sismics.music.core.model.dbi.User;

/**
 * Event raised after the creation of a user.
 *
 * @author jtremeaux 
 */
public class UserCreatedEvent {
    /**
     * Created user.
     */
    private User user;

    /**
     * Getter of user.
     *
     * @return user
     */
    public User getUser() {
        return user;
    }

    /**
     * Setter of user.
     *
     * @param user user
     */
    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("user", user)
                .toString();
    }
}
