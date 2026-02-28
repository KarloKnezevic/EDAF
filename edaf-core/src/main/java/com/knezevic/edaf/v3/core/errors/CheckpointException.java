/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.core.errors;

/**
 * Thrown when checkpoint save/load fails.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public class CheckpointException extends EdafException {

    /**
     * Creates a new CheckpointException instance.
     *
     * @param message message text
     * @param cause exception cause
     */
    public CheckpointException(String message, Throwable cause) {
        super(message, cause);
    }
}
