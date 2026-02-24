/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.core.plugins;

import com.knezevic.edaf.v3.core.api.Representation;

import java.util.Map;

/**
 * Plugin factory for representation components.
 */
public interface RepresentationPlugin<G> extends Plugin {

    /**
     * Constructs a representation instance from config parameters.
     */
    Representation<G> create(Map<String, Object> params);
}
