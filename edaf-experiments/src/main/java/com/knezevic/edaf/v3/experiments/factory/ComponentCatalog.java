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
 */
public final class ComponentCatalog {

    private final PluginRegistry registry;

    public ComponentCatalog() {
        this.registry = new PluginRegistry().discoverFromClasspath();
    }

    @SuppressWarnings("unchecked")
    public <G> Representation<G> createRepresentation(ExperimentConfig config) {
        var plugin = registry.<G>representation(config.getRepresentation().getType());
        return plugin.create(paramsWithGrammar(config, config.getRepresentation().getParams()));
    }

    @SuppressWarnings("unchecked")
    public <G> Problem<G> createProblem(ExperimentConfig config) {
        var plugin = registry.<G>problem(config.getProblem().getType());
        return plugin.create(paramsWithGrammar(config, config.getProblem().getParams()));
    }

    @SuppressWarnings("unchecked")
    public <G> Model<G> createModel(ExperimentConfig config) {
        var plugin = registry.<G>model(config.getModel().getType());
        return plugin.create(paramsWithGrammar(config, config.getModel().getParams()));
    }

    @SuppressWarnings("unchecked")
    public <G> Algorithm<G> createAlgorithm(ExperimentConfig config, AlgorithmDependencies<G> dependencies) {
        var plugin = registry.<G>algorithm(config.getAlgorithm().getType());
        return plugin.create(dependencies, paramsWithGrammar(config, config.getAlgorithm().getParams()));
    }

    public List<Plugin> listAlgorithms() {
        return registry.listAlgorithms();
    }

    public List<Plugin> listModels() {
        return registry.listModels();
    }

    public List<Plugin> listProblems() {
        return registry.listProblems();
    }

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
