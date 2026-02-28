/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.experiments.factory;

import com.knezevic.edaf.v3.core.api.Algorithm;
import com.knezevic.edaf.v3.core.api.Model;
import com.knezevic.edaf.v3.core.api.Problem;
import com.knezevic.edaf.v3.core.api.Representation;
import com.knezevic.edaf.v3.core.config.ExperimentConfig;
import com.knezevic.edaf.v3.core.plugins.AlgorithmDependencies;
import com.knezevic.edaf.v3.core.plugins.Plugin;
import com.knezevic.edaf.v3.core.plugins.PluginRegistry;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Runtime component catalog backed by ServiceLoader-discovered plugins.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class ComponentCatalog {

    private final PluginRegistry registry;

    /**
     * Creates a new ComponentCatalog instance.
     */
    public ComponentCatalog() {
        this.registry = new PluginRegistry().discoverFromClasspath();
    }

    /**
     * Creates a representation instance from the representation section.
     *
     * @param config experiment configuration
     * @return instantiated representation
     */
    @SuppressWarnings("unchecked")
    public <G> Representation<G> createRepresentation(ExperimentConfig config) {
        var plugin = registry.<G>representation(config.getRepresentation().getType());
        return plugin.create(paramsWithGrammar(config, config.getRepresentation().getParams()));
    }

    /**
     * Creates a problem instance from the problem section.
     *
     * @param config experiment configuration
     * @return instantiated problem
     */
    @SuppressWarnings("unchecked")
    public <G> Problem<G> createProblem(ExperimentConfig config) {
        var plugin = registry.<G>problem(config.getProblem().getType());
        return plugin.create(paramsWithGrammar(config, config.getProblem().getParams()));
    }

    /**
     * Creates a probabilistic model from the model section.
     *
     * @param config experiment configuration
     * @return instantiated model
     */
    @SuppressWarnings("unchecked")
    public <G> Model<G> createModel(ExperimentConfig config) {
        var plugin = registry.<G>model(config.getModel().getType());
        return plugin.create(paramsWithGrammar(config, config.getModel().getParams()));
    }

    /**
     * Creates an algorithm driver bound to resolved runtime dependencies.
     *
     * @param config experiment configuration
     * @param dependencies dependency bundle
     * @return instantiated algorithm
     */
    @SuppressWarnings("unchecked")
    public <G> Algorithm<G> createAlgorithm(ExperimentConfig config, AlgorithmDependencies<G> dependencies) {
        var plugin = registry.<G>algorithm(config.getAlgorithm().getType());
        return plugin.create(dependencies, paramsWithGrammar(config, config.getAlgorithm().getParams()));
    }

    /**
     * Lists all registered algorithm plugins.
     *
     * @return discovered algorithm plugins
     */
    public List<Plugin> listAlgorithms() {
        return registry.listAlgorithms();
    }

    /**
     * Lists all registered model plugins.
     *
     * @return discovered model plugins
     */
    public List<Plugin> listModels() {
        return registry.listModels();
    }

    /**
     * Lists all registered problem plugins.
     *
     * @return discovered problem plugins
     */
    public List<Plugin> listProblems() {
        return registry.listProblems();
    }

    /**
     * Lists all registered representation plugins.
     *
     * @return discovered representation plugins
     */
    public List<Plugin> listRepresentations() {
        return registry.listRepresentations();
    }

    private static Map<String, Object> paramsWithGrammar(ExperimentConfig config, Map<String, Object> sectionParams) {
        Map<String, Object> merged = new LinkedHashMap<>();
        if (sectionParams != null) {
            merged.putAll(sectionParams);
        }
        if (!merged.containsKey("grammar")
                && config != null
                && config.getGrammar() != null
                && !config.getGrammar().getOptions().isEmpty()) {
            merged.put("grammar", new LinkedHashMap<>(config.getGrammar().getOptions()));
        }
        return merged;
    }
}
