package com.knezevic.edaf.v3.core.plugins;

import com.knezevic.edaf.v3.core.api.Problem;

import java.util.Map;

/**
 * Plugin factory for optimization problems.
 */
public interface ProblemPlugin<G> extends Plugin {

    /**
     * Constructs a problem instance from config parameters.
     */
    Problem<G> create(Map<String, Object> params);
}
