/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.core.plugins;

import com.knezevic.edaf.v3.core.api.Model;

import java.util.Map;

/**
 * Plugin factory for probabilistic models.
 */
public interface ModelPlugin<G> extends Plugin {

    /**
     * Constructs a model instance from config parameters.
     */
    Model<G> create(Map<String, Object> params);
}
