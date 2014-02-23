package com.sismics.music.event;

/**
 * My music fragment menu visibility event.
 *
 * @author bgamard.
 */
public class MyMusicMenuVisibilityChanged {

    boolean menuVisible;

    public MyMusicMenuVisibilityChanged(boolean menuVisible) {
        this.menuVisible = menuVisible;
    }

    public boolean isMenuVisible() {
        return menuVisible;
    }
}
