/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.core.plugins;

import com.knezevic.edaf.v3.core.api.Problem;

import java.util.Map;

/**
 * Plugin factory for optimization problems.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public interface ProblemPlugin<G> extends Plugin {

    /**
     * Constructs a problem instance from config the input values.
     */
    Problem<G> create(Map<String, Object> params);
}
