/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.core.api;

import com.knezevic.edaf.v3.core.rng.RngStream;

import java.util.List;

/**
 * Probabilistic model contract used by EDA-style algorithms.
 *
 * @param <G> genotype value type.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public interface Model<G> {

    /**
     * Returns the model identifier used in configuration and diagnostics.
     *
     * @return model identifier
     */
    String name();

    /**
     * Fits model the input values from selected individuals.
     *
     * @param selected selected individuals used for model estimation
     * @param representation genotype representation used for validation and repair
     * @param rng random stream dedicated to model fitting
     */
    void fit(List<Individual<G>> selected, Representation<G> representation, RngStream rng);

    /**
     * Updates model the input values incrementally for one individual when supported.
     *
     * @param individual individual used for incremental update
     * @param learningRate update learning rate
     * @param representation genotype representation used for validation and repair
     * @param rng random stream dedicated to model updates
     */
    default void update(Individual<G> individual, double learningRate, Representation<G> representation, RngStream rng) {
        // default no-op for batch-fit models
    }

    /**
     * Samples new genotypes from the fitted model.
     *
     * @param count number of genotypes to sample
     * @param representation genotype representation used for validation and repair
     * @param problem optimization problem for constraint context
     * @param constraintHandling configured constraint handling strategy
     * @param rng random stream dedicated to model sampling
     * @return sampled genotype list
     */
    List<G> sample(int count,
                   Representation<G> representation,
                   Problem<G> problem,
                   ConstraintHandling<G> constraintHandling,
                   RngStream rng);

    /**
     * Returns model diagnostics such as entropy or covariance conditioning.
     *
     * @return diagnostics snapshot
     */
    default ModelDiagnostics diagnostics() {
        return ModelDiagnostics.empty();
    }
}
