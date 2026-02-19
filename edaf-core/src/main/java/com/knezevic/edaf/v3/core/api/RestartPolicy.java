package com.knezevic.edaf.v3.core.api;

import com.knezevic.edaf.v3.core.rng.RngStream;

/**
 * Policy controlling run restarts when stagnation or degeneracy is detected.
 */
public interface RestartPolicy<G> {

    /**
     * Returns true when a restart should happen.
     */
    boolean shouldRestart(AlgorithmState<G> state);

    /**
     * Builds a restarted population.
     */
    Population<G> restart(AlgorithmState<G> state, Representation<G> representation, RngStream rng);

    /**
     * Policy identifier used in logs.
     */
    String name();
}
