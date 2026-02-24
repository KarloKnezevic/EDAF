/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.core.errors;

/**
 * Thrown when checkpoint save/load fails.
 */
public class CheckpointException extends EdafException {

    public CheckpointException(String message, Throwable cause) {
        super(message, cause);
    }
}
