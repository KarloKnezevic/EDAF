/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.core.errors;

/**
 * Thrown when configuration parsing or validation fails.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public class ConfigurationException extends EdafException {

    /**
     * Creates a new ConfigurationException instance.
     *
     * @param message message text
     */
    public ConfigurationException(String message) {
        super(message);
    }

    /**
     * Creates a new ConfigurationException instance.
     *
     * @param message message text
     * @param cause exception cause
     */
    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
