package com.sismics.music.core.exception;

/**
 * Exception raised when a folder is not writable.
 * 
 * @author bgamard
 */
public class NonWritableException extends Exception {

    private static final long serialVersionUID = 1L;

    public NonWritableException(Throwable cause) {
        super(cause);
    }
}
