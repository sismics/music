package com.sismics.music.event;

/**
 * Media player seeking event.
 *
 * @author bgamard.
 */
public class MediaPlayerSeekEvent {

    private int position;

    public MediaPlayerSeekEvent(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }
}
