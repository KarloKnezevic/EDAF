/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.core.api.defaults;

import com.knezevic.edaf.v3.core.api.NichingPolicy;
import com.knezevic.edaf.v3.core.api.Population;
import com.knezevic.edaf.v3.core.api.Representation;
import com.knezevic.edaf.v3.core.rng.RngStream;

/**
 * Default niching policy that preserves the original population.
 *
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class NoNichingPolicy<G> implements NichingPolicy<G> {

    /**
     * Returns population unchanged.
     *
     * @param population population to process
     * @param representation genotype representation
     * @param rng random stream
     * @return unchanged population
     */
    @Override
    public Population<G> apply(Population<G> population, Representation<G> representation, RngStream rng) {
        return population;
    }

    /**
     * Returns niching-policy identifier.
     *
     * @return policy identifier
     */
    @Override
    public String name() {
        return "none";
    }
}
