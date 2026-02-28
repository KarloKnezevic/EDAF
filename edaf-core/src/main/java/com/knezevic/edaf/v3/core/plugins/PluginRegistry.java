/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.core.plugins;

import com.knezevic.edaf.v3.core.errors.ComponentResolutionException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;

/**
 * Runtime plugin registry with optional ServiceLoader discovery.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class PluginRegistry {

    private final Map<String, RepresentationPlugin<?>> representations = new LinkedHashMap<>();
    private final Map<String, ProblemPlugin<?>> problems = new LinkedHashMap<>();
    private final Map<String, ModelPlugin<?>> models = new LinkedHashMap<>();
    private final Map<String, AlgorithmPlugin<?>> algorithms = new LinkedHashMap<>();

    /**
     * Executes discover from classpath.
     *
     * @return the discover from classpath
     */
    public PluginRegistry discoverFromClasspath() {
        ServiceLoader.load(RepresentationPlugin.class).forEach(p -> registerRepresentation((RepresentationPlugin<?>) p));
        ServiceLoader.load(ProblemPlugin.class).forEach(p -> registerProblem((ProblemPlugin<?>) p));
        ServiceLoader.load(ModelPlugin.class).forEach(p -> registerModel((ModelPlugin<?>) p));
        ServiceLoader.load(AlgorithmPlugin.class).forEach(p -> registerAlgorithm((AlgorithmPlugin<?>) p));
        return this;
    }

    /**
     * Executes register representation.
     *
     * @param plugin the plugin argument
     * @return the register representation
     */
    public PluginRegistry registerRepresentation(RepresentationPlugin<?> plugin) {
        representations.put(normalize(plugin.type()), Objects.requireNonNull(plugin, "plugin must not be null"));
        return this;
    }

    /**
     * Executes register problem.
     *
     * @param plugin the plugin argument
     * @return the register problem
     */
    public PluginRegistry registerProblem(ProblemPlugin<?> plugin) {
        problems.put(normalize(plugin.type()), Objects.requireNonNull(plugin, "plugin must not be null"));
        return this;
    }

    /**
     * Executes register model.
     *
     * @param plugin the plugin argument
     * @return the register model
     */
    public PluginRegistry registerModel(ModelPlugin<?> plugin) {
        models.put(normalize(plugin.type()), Objects.requireNonNull(plugin, "plugin must not be null"));
        return this;
    }

    /**
     * Executes register algorithm.
     *
     * @param plugin the plugin argument
     * @return the register algorithm
     */
    public PluginRegistry registerAlgorithm(AlgorithmPlugin<?> plugin) {
        algorithms.put(normalize(plugin.type()), Objects.requireNonNull(plugin, "plugin must not be null"));
        return this;
    }

    @SuppressWarnings("unchecked")
    /**
     * Executes representation.
     *
     * @param type the type argument
     * @return the representation
     */
    public <G> RepresentationPlugin<G> representation(String type) {
        RepresentationPlugin<?> plugin = representations.get(normalize(type));
        if (plugin == null) {
            throw new ComponentResolutionException("Unknown representation type: " + type);
        }
        return (RepresentationPlugin<G>) plugin;
    }

    @SuppressWarnings("unchecked")
    /**
     * Executes problem.
     *
     * @param type the type argument
     * @return the problem
     */
    public <G> ProblemPlugin<G> problem(String type) {
        ProblemPlugin<?> plugin = problems.get(normalize(type));
        if (plugin == null) {
            throw new ComponentResolutionException("Unknown problem type: " + type);
        }
        return (ProblemPlugin<G>) plugin;
    }

    @SuppressWarnings("unchecked")
    /**
     * Executes model.
     *
     * @param type the type argument
     * @return the model
     */
    public <G> ModelPlugin<G> model(String type) {
        ModelPlugin<?> plugin = models.get(normalize(type));
        if (plugin == null) {
            throw new ComponentResolutionException("Unknown model type: " + type);
        }
        return (ModelPlugin<G>) plugin;
    }

    @SuppressWarnings("unchecked")
    /**
     * Executes algorithm.
     *
     * @param type the type argument
     * @return the algorithm
     */
    public <G> AlgorithmPlugin<G> algorithm(String type) {
        AlgorithmPlugin<?> plugin = algorithms.get(normalize(type));
        if (plugin == null) {
            throw new ComponentResolutionException("Unknown algorithm type: " + type);
        }
        return (AlgorithmPlugin<G>) plugin;
    }

    /**
     * Lists representations.
     *
     * @return the list representations
     */
    public List<Plugin> listRepresentations() {
        return new ArrayList<>(representations.values());
    }

    /**
     * Lists problems.
     *
     * @return the list problems
     */
    public List<Plugin> listProblems() {
        return new ArrayList<>(problems.values());
    }

    /**
     * Lists models.
     *
     * @return the list models
     */
    public List<Plugin> listModels() {
        return new ArrayList<>(models.values());
    }

    /**
     * Lists algorithms.
     *
     * @return the list algorithms
     */
    public List<Plugin> listAlgorithms() {
        return new ArrayList<>(algorithms.values());
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}
