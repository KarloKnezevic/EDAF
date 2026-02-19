package com.knezevic.edaf.v3.core.plugins;

import com.knezevic.edaf.v3.core.api.Algorithm;

import java.util.Map;

/**
 * Plugin factory for algorithms.
 */
public interface AlgorithmPlugin<G> extends Plugin {

    /**
     * Constructs an algorithm instance from dependencies and config parameters.
     */
    Algorithm<G> create(AlgorithmDependencies<G> dependencies, Map<String, Object> params);
}
