/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.core.api;

/**
 * Lifecycle contract for optimization algorithms.
 *
 * @param <G> genotype value type.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public interface Algorithm<G> {

    /**
     * Returns the unique algorithm identifier used in configuration and persistence.
     *
     * @return algorithm identifier
     */
    String id();

    /**
     * Initializes algorithm state before the first optimization iteration.
     *
     * @param context immutable runtime context with all run components
     */
    void initialize(AlgorithmContext<G> context);

    /**
     * Executes a single algorithm iteration and updates internal state.
     *
     * @param context immutable runtime context with all run components
     */
    void iterate(AlgorithmContext<G> context);

    /**
     * Returns the current optimization state snapshot.
     *
     * @return current algorithm state
     */
    AlgorithmState<G> state();

    /**
     * Returns the final run summary after completion.
     *
     * @return immutable run result
     */
    RunResult<G> result();

    /**
     * Runs the full lifecycle until the stopping condition triggers.
     *
     * @param context immutable runtime context with all run components
     */
    void run(AlgorithmContext<G> context);
}
