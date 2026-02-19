package com.knezevic.edaf.v3.core.errors;

/**
 * Thrown when persistence sinks cannot store run data.
 */
public class PersistenceException extends EdafException {

    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
