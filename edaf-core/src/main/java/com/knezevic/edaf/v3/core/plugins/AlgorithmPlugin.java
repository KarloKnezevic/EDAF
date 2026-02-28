/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.core.plugins;

import com.knezevic.edaf.v3.core.api.Algorithm;

import java.util.Map;

/**
 * Plugin factory for algorithms.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public interface AlgorithmPlugin<G> extends Plugin {

    /**
     * Constructs an algorithm instance from dependencies and config the input values.
     */
    Algorithm<G> create(AlgorithmDependencies<G> dependencies, Map<String, Object> params);
}
