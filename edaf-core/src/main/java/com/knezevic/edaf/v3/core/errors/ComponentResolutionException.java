/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.core.errors;

/**
 * Thrown when a requested plugin/component is not registered or incompatible.
 */
public class ComponentResolutionException extends EdafException {

    public ComponentResolutionException(String message) {
        super(message);
    }
}
