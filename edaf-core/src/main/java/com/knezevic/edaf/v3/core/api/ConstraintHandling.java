package com.knezevic.edaf.v3.core.api;

import com.knezevic.edaf.v3.core.rng.RngStream;

/**
 * Strategy for handling infeasible sampled genotypes.
 */
public interface ConstraintHandling<G> {

    /**
     * Repairs or rejects-and-resamples candidate solutions.
     */
    G enforce(G candidate, Representation<G> representation, Problem<G> problem, RngStream rng);

    /**
     * Strategy identifier used in diagnostics.
     */
    String name();
}
