/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.core.api;

import com.knezevic.edaf.v3.core.rng.RngStream;

/**
 * Strategy for handling infeasible sampled genotypes.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
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
