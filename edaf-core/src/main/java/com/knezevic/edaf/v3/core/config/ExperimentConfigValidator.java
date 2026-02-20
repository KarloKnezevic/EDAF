package com.knezevic.edaf.v3.core.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Semantic validator for v3 configs with actionable messages.
 */
public final class ExperimentConfigValidator {

    private static final Set<String> DISCRETE_REPRESENTATIONS = Set.of(
            "bitstring", "int-vector", "categorical-vector", "mixed-discrete-vector", "variable-length-vector");
    private static final Set<String> CONTINUOUS_REPRESENTATIONS = Set.of(
            "real-vector", "mixed-real-discrete-vector");
    private static final Set<String> PERMUTATION_REPRESENTATIONS = Set.of("permutation-vector");

    private static final Set<String> DISCRETE_MODELS = Set.of(
            "umda-bernoulli", "pbil-frequency", "cga-frequency", "bmda", "mimic-chow-liu", "boa-ebna",
            "hboa-network", "token-categorical");
    private static final Set<String> CONTINUOUS_MODELS = Set.of(
            "gaussian-diag", "gaussian-full", "gmm", "kde", "copula-baseline", "snes", "xnes", "cma-es",
            "normalizing-flow");
    private static final Set<String> PERMUTATION_MODELS = Set.of("ehm", "plackett-luce", "mallows");

    private static final Set<String> DISCRETE_ALGORITHMS = Set.of(
            "umda", "pbil", "cga", "bmda", "mimic", "boa", "ebna",
            "hboa", "factorized-discrete-eda", "dependency-tree-eda", "chow-liu-eda",
            "mo-eda-skeleton", "pareto-eda", "indicator-eda", "tree-eda",
            "sliding-window-eda", "memory-eda", "random-immigrants-eda", "noisy-resampling-eda");
    private static final Set<String> CONTINUOUS_ALGORITHMS = Set.of(
            "gaussian-eda", "gmm-eda", "kde-eda", "copula-eda", "snes", "xnes", "cma-es",
            "cem", "umda-continuous", "pbil-real", "mimic-continuous",
            "full-covariance-eda", "lowrank-covariance-eda", "block-covariance-eda", "flow-eda", "igo",
            "mo-eda-skeleton", "pareto-eda", "indicator-eda",
            "sliding-window-eda", "memory-eda", "random-immigrants-eda", "noisy-resampling-eda");
    private static final Set<String> PERMUTATION_ALGORITHMS = Set.of(
            "ehm-eda", "ehbsa", "position-based-permutation-eda", "kendall-permutation-eda",
            "plackett-luce-eda", "mallows-eda",
            "mo-eda-skeleton", "pareto-eda", "indicator-eda",
            "sliding-window-eda", "memory-eda", "random-immigrants-eda", "noisy-resampling-eda");

    private static final Set<String> SUPPORTED_LOGGING_MODES = Set.of("console", "jsonl", "file", "db");
    private static final Set<String> SUPPORTED_PERSISTENCE_SINKS = Set.of("console", "csv", "jsonl", "file", "db");

    /**
     * Validates cross-field semantics and returns accumulated issues.
     */
    public List<ConfigIssue> validate(ExperimentConfig config) {
        List<ConfigIssue> issues = new ArrayList<>();

        String schema = normalize(config.getSchema());
        if (!schema.startsWith("3.")) {
            issues.add(new ConfigIssue(
                    "schema",
                    "Unsupported schema version '" + config.getSchema() + "'",
                    "Use schema: \"3.0\"."
            ));
        }

        String representation = normalize(config.getRepresentation().getType());
        String model = normalize(config.getModel().getType());
        String algorithm = normalize(config.getAlgorithm().getType());

        if (DISCRETE_REPRESENTATIONS.contains(representation)) {
            validateMembership(model, DISCRETE_MODELS, "model.type", issues,
                    "Discrete representations require one of: " + DISCRETE_MODELS);
            validateMembership(algorithm, DISCRETE_ALGORITHMS, "algorithm.type", issues,
                    "Discrete representations require one of: " + DISCRETE_ALGORITHMS);
        } else if (CONTINUOUS_REPRESENTATIONS.contains(representation)) {
            validateMembership(model, CONTINUOUS_MODELS, "model.type", issues,
                    "Continuous representations require one of: " + CONTINUOUS_MODELS);
            validateMembership(algorithm, CONTINUOUS_ALGORITHMS, "algorithm.type", issues,
                    "Continuous representations require one of: " + CONTINUOUS_ALGORITHMS);
        } else if (PERMUTATION_REPRESENTATIONS.contains(representation)) {
            validateMembership(model, PERMUTATION_MODELS, "model.type", issues,
                    "Permutation representations require one of: " + PERMUTATION_MODELS);
            validateMembership(algorithm, PERMUTATION_ALGORITHMS, "algorithm.type", issues,
                    "Permutation representations require one of: " + PERMUTATION_ALGORITHMS);
        } else {
            issues.add(new ConfigIssue(
                    "representation.type",
                    "Unknown representation type '" + config.getRepresentation().getType() + "'",
                    "Use bitstring/int-vector/real-vector/permutation-vector/..."
            ));
        }

        String stoppingType = normalize(config.getStopping().getType());
        if (!"max-iterations".equals(stoppingType)) {
            issues.add(new ConfigIssue(
                    "stopping.type",
                    "Unsupported stopping type '" + config.getStopping().getType() + "'",
                    "Currently supported: max-iterations"
            ));
        }

        if (config.getLogging().getModes() == null || config.getLogging().getModes().isEmpty()) {
            issues.add(new ConfigIssue(
                    "logging.modes",
                    "At least one logging mode is required",
                    "Use: [console], [console, jsonl], [file], [db], ..."
            ));
        } else {
            for (String mode : config.getLogging().getModes()) {
                String normalized = normalize(mode);
                if (!SUPPORTED_LOGGING_MODES.contains(normalized)) {
                    issues.add(new ConfigIssue(
                            "logging.modes",
                            "Unsupported logging mode '" + mode + "'",
                            "Supported values: " + SUPPORTED_LOGGING_MODES
                    ));
                }
            }
        }

        if (config.getPersistence().getSinks() == null || config.getPersistence().getSinks().isEmpty()) {
            issues.add(new ConfigIssue(
                    "persistence.sinks",
                    "At least one persistence sink is required",
                    "Use: [console], [csv], [jsonl], [db], ..."
            ));
        } else {
            for (String sink : config.getPersistence().getSinks()) {
                String normalized = normalize(sink);
                if (!SUPPORTED_PERSISTENCE_SINKS.contains(normalized)) {
                    issues.add(new ConfigIssue(
                            "persistence.sinks",
                            "Unsupported persistence sink '" + sink + "'",
                            "Supported values: " + SUPPORTED_PERSISTENCE_SINKS
                    ));
                }
                if ("db".equals(normalized) && !config.getPersistence().getDatabase().isEnabled()) {
                    issues.add(new ConfigIssue(
                            "persistence.database.enabled",
                            "DB sink requested but database.enabled is false",
                            "Set persistence.database.enabled: true or remove db sink"
                    ));
                }
            }
        }

        String verbosity = normalize(config.getLogging().getVerbosity());
        if (!Set.of("quiet", "normal", "verbose", "debug").contains(verbosity)) {
            issues.add(new ConfigIssue(
                    "logging.verbosity",
                    "Unsupported verbosity '" + config.getLogging().getVerbosity() + "'",
                    "Use one of: quiet, normal, verbose, debug"
            ));
        }

        return issues;
    }

    private static void validateMembership(String actual,
                                           Set<String> allowed,
                                           String path,
                                           List<ConfigIssue> issues,
                                           String hint) {
        if (!allowed.contains(actual)) {
            issues.add(new ConfigIssue(path, "Incompatible value: '" + actual + "'", hint));
        }
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
