/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.core.errors;

/**
 * Base runtime exception for EDAF v3.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public class EdafException extends RuntimeException {

    /**
     * Creates a new EdafException instance.
     *
     * @param message message text
     */
    public EdafException(String message) {
        super(message);
    }

    /**
     * Creates a new EdafException instance.
     *
     * @param message message text
     * @param cause exception cause
     */
    public EdafException(String message, Throwable cause) {
        super(message, cause);
    }
}
