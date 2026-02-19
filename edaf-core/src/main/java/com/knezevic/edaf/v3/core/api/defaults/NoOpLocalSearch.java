package com.knezevic.edaf.v3.core.api.defaults;

import com.knezevic.edaf.v3.core.api.Individual;
import com.knezevic.edaf.v3.core.api.LocalSearch;
import com.knezevic.edaf.v3.core.api.Problem;
import com.knezevic.edaf.v3.core.api.Representation;
import com.knezevic.edaf.v3.core.rng.RngStream;

/**
 * Default local-search strategy that leaves individuals unchanged.
 */
public final class NoOpLocalSearch<G> implements LocalSearch<G> {

    @Override
    public Individual<G> refine(Individual<G> individual, Problem<G> problem, Representation<G> representation, RngStream rng) {
        return individual;
    }

    @Override
    public String name() {
        return "none";
    }
}
