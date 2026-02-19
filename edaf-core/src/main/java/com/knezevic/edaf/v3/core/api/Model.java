package com.knezevic.edaf.v3.core.api;

import com.knezevic.edaf.v3.core.rng.RngStream;

import java.util.List;

/**
 * Probabilistic model contract used by EDA-style algorithms.
 *
 * @param <G> genotype value type.
 */
public interface Model<G> {

    /**
     * Model identifier used in configuration and listing commands.
     */
    String name();

    /**
     * Fits model parameters from selected individuals.
     */
    void fit(List<Individual<G>> selected, Representation<G> representation, RngStream rng);

    /**
     * Optional online update for incremental models.
     */
    default void update(Individual<G> individual, double learningRate, Representation<G> representation, RngStream rng) {
        // default no-op for batch-fit models
    }

    /**
     * Samples new genotypes from model.
     */
    List<G> sample(int count,
                   Representation<G> representation,
                   Problem<G> problem,
                   ConstraintHandling<G> constraintHandling,
                   RngStream rng);

    /**
     * Exposes diagnostics (entropy, covariance condition number, etc.).
     */
    default ModelDiagnostics diagnostics() {
        return ModelDiagnostics.empty();
    }
}
