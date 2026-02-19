package com.knezevic.edaf.v3.core.errors;

/**
 * Base runtime exception for EDAF v3.
 */
public class EdafException extends RuntimeException {

    public EdafException(String message) {
        super(message);
    }

    public EdafException(String message, Throwable cause) {
        super(message, cause);
    }
}
