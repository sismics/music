package com.sismics.music.event;

/**
 * Offline mode changed event.
 *
 * @author bgamard.
 */
public class OfflineModeChangedEvent {

    private boolean offlineMode;

    public OfflineModeChangedEvent(boolean offlineMode) {
        this.offlineMode = offlineMode;
    }

    public boolean isOfflineMode() {
        return offlineMode;
    }
}
