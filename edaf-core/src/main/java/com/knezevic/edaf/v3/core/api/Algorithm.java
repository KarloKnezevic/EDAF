package com.knezevic.edaf.v3.core.api;

/**
 * Lifecycle contract for optimization algorithms.
 *
 * @param <G> genotype value type.
 */
public interface Algorithm<G> {

    /**
     * Algorithm identifier (e.g. umda, gaussian-diag-eda, ehm-eda).
     */
    String id();

    /**
     * Initializes state before iterations start.
     */
    void initialize(AlgorithmContext<G> context);

    /**
     * Executes one iteration.
     */
    void iterate(AlgorithmContext<G> context);

    /**
     * Returns current state snapshot.
     */
    AlgorithmState<G> state();

    /**
     * Returns final result after run completion.
     */
    RunResult<G> result();

    /**
     * Runs initialize + iterate loop until stopping condition triggers.
     */
    void run(AlgorithmContext<G> context);
}
