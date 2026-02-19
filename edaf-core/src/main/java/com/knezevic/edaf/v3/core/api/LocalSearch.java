package com.knezevic.edaf.v3.core.api;

import com.knezevic.edaf.v3.core.rng.RngStream;

/**
 * Optional local-search hook for memetic EDA variants.
 */
public interface LocalSearch<G> {

    /**
     * Applies local search and returns the refined individual.
     */
    Individual<G> refine(Individual<G> individual, Problem<G> problem, Representation<G> representation, RngStream rng);

    /**
     * Hook identifier used in logs.
     */
    String name();
}
