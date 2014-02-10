package com.sismics.music.core.event.async;

import com.google.common.base.Objects;
import com.sismics.music.core.model.dbi.Directory;

/**
 * Directory deleted event.
 *
 * @author jtremeaux
 */
public class DirectoryDeletedAsyncEvent {
    /**
     * New directory.
     */
    private Directory directory;

    /**
     * Getter of directory.
     *
     * @return directory
     */
    public Directory getDirectory() {
        return directory;
    }

    /**
     * Setter of directory.
     *
     * @param directory directory
     */
    public void setDirectory(Directory directory) {
        this.directory = directory;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("directory", directory)
                .toString();
    }
}
