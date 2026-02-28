/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.core.errors;

/**
 * Thrown when a requested plugin/component is not registered or incompatible.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public class ComponentResolutionException extends EdafException {

    /**
     * Creates a new ComponentResolutionException instance.
     *
     * @param message message text
     */
    public ComponentResolutionException(String message) {
        super(message);
    }
}
