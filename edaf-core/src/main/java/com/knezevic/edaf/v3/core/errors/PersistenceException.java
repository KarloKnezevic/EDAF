/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.core.errors;

/**
 * Thrown when persistence sinks cannot store run data.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public class PersistenceException extends EdafException {

    /**
     * Creates a new PersistenceException instance.
     *
     * @param message message text
     * @param cause exception cause
     */
    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
