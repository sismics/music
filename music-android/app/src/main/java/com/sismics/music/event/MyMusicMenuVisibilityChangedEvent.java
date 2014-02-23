package com.sismics.music.event;

/**
 * My music fragment menu visibility event.
 *
 * @author bgamard.
 */
public class MyMusicMenuVisibilityChangedEvent {

    private boolean menuVisible;

    public MyMusicMenuVisibilityChangedEvent(boolean menuVisible) {
        this.menuVisible = menuVisible;
    }

    public boolean isMenuVisible() {
        return menuVisible;
    }
}
