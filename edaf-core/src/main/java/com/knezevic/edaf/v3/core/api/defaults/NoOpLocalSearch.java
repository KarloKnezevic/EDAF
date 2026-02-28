/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.core.api.defaults;

import com.knezevic.edaf.v3.core.api.Individual;
import com.knezevic.edaf.v3.core.api.LocalSearch;
import com.knezevic.edaf.v3.core.api.Problem;
import com.knezevic.edaf.v3.core.api.Representation;
import com.knezevic.edaf.v3.core.rng.RngStream;

/**
 * Default local-search strategy that leaves individuals unchanged.
 *
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class NoOpLocalSearch<G> implements LocalSearch<G> {

    /**
     * Returns individual unchanged.
     *
     * @param individual individual to refine
     * @param problem optimization problem
     * @param representation genotype representation
     * @param rng random stream
     * @return unchanged individual
     */
    @Override
    public Individual<G> refine(Individual<G> individual, Problem<G> problem, Representation<G> representation, RngStream rng) {
        return individual;
    }

    /**
     * Returns local-search identifier.
     *
     * @return local-search identifier
     */
    @Override
    public String name() {
        return "none";
    }
}
