/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.core.api;

import com.knezevic.edaf.v3.core.rng.RngStream;

/**
 * Policy for diversity preservation and niche formation.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public interface NichingPolicy<G> {

    /**
     * Applies niching post-processing to a population.
     */
    Population<G> apply(Population<G> population, Representation<G> representation, RngStream rng);

    /**
     * Policy identifier used in logs.
     */
    String name();
}
