/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.core.plugins;

import com.knezevic.edaf.v3.core.api.Model;

import java.util.Map;

/**
 * Plugin factory for probabilistic models.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public interface ModelPlugin<G> extends Plugin {

    /**
     * Constructs a model instance from config the input values.
     */
    Model<G> create(Map<String, Object> params);
}
