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
 */
public final class PluginRegistry {

    private final Map<String, RepresentationPlugin<?>> representations = new LinkedHashMap<>();
    private final Map<String, ProblemPlugin<?>> problems = new LinkedHashMap<>();
    private final Map<String, ModelPlugin<?>> models = new LinkedHashMap<>();
    private final Map<String, AlgorithmPlugin<?>> algorithms = new LinkedHashMap<>();

    public PluginRegistry discoverFromClasspath() {
        ServiceLoader.load(RepresentationPlugin.class).forEach(p -> registerRepresentation((RepresentationPlugin<?>) p));
        ServiceLoader.load(ProblemPlugin.class).forEach(p -> registerProblem((ProblemPlugin<?>) p));
        ServiceLoader.load(ModelPlugin.class).forEach(p -> registerModel((ModelPlugin<?>) p));
        ServiceLoader.load(AlgorithmPlugin.class).forEach(p -> registerAlgorithm((AlgorithmPlugin<?>) p));
        return this;
    }

    public PluginRegistry registerRepresentation(RepresentationPlugin<?> plugin) {
        representations.put(normalize(plugin.type()), Objects.requireNonNull(plugin, "plugin must not be null"));
        return this;
    }

    public PluginRegistry registerProblem(ProblemPlugin<?> plugin) {
        problems.put(normalize(plugin.type()), Objects.requireNonNull(plugin, "plugin must not be null"));
        return this;
    }

    public PluginRegistry registerModel(ModelPlugin<?> plugin) {
        models.put(normalize(plugin.type()), Objects.requireNonNull(plugin, "plugin must not be null"));
        return this;
    }

    public PluginRegistry registerAlgorithm(AlgorithmPlugin<?> plugin) {
        algorithms.put(normalize(plugin.type()), Objects.requireNonNull(plugin, "plugin must not be null"));
        return this;
    }

    @SuppressWarnings("unchecked")
    public <G> RepresentationPlugin<G> representation(String type) {
        RepresentationPlugin<?> plugin = representations.get(normalize(type));
        if (plugin == null) {
            throw new ComponentResolutionException("Unknown representation type: " + type);
        }
        return (RepresentationPlugin<G>) plugin;
    }

    @SuppressWarnings("unchecked")
    public <G> ProblemPlugin<G> problem(String type) {
        ProblemPlugin<?> plugin = problems.get(normalize(type));
        if (plugin == null) {
            throw new ComponentResolutionException("Unknown problem type: " + type);
        }
        return (ProblemPlugin<G>) plugin;
    }

    @SuppressWarnings("unchecked")
    public <G> ModelPlugin<G> model(String type) {
        ModelPlugin<?> plugin = models.get(normalize(type));
        if (plugin == null) {
            throw new ComponentResolutionException("Unknown model type: " + type);
        }
        return (ModelPlugin<G>) plugin;
    }

    @SuppressWarnings("unchecked")
    public <G> AlgorithmPlugin<G> algorithm(String type) {
        AlgorithmPlugin<?> plugin = algorithms.get(normalize(type));
        if (plugin == null) {
            throw new ComponentResolutionException("Unknown algorithm type: " + type);
        }
        return (AlgorithmPlugin<G>) plugin;
    }

    public List<Plugin> listRepresentations() {
        return new ArrayList<>(representations.values());
    }

    public List<Plugin> listProblems() {
        return new ArrayList<>(problems.values());
    }

    public List<Plugin> listModels() {
        return new ArrayList<>(models.values());
    }

    public List<Plugin> listAlgorithms() {
        return new ArrayList<>(algorithms.values());
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}
