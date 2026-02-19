package com.knezevic.edaf.v3.core.errors;

/**
 * Thrown when checkpoint save/load fails.
 */
public class CheckpointException extends EdafException {

    public CheckpointException(String message, Throwable cause) {
        super(message, cause);
    }
}
