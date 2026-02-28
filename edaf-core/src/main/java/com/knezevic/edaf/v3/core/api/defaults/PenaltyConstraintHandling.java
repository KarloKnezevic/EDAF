/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.core.api.defaults;

import com.knezevic.edaf.v3.core.api.ConstraintHandling;
import com.knezevic.edaf.v3.core.api.Problem;
import com.knezevic.edaf.v3.core.api.Representation;
import com.knezevic.edaf.v3.core.rng.RngStream;

/**
 * Penalty-style constraint handling scaffold.
 *
 * TODO(priority=medium): Integrate explicit penalty terms into fitness pipeline.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class PenaltyConstraintHandling<G> implements ConstraintHandling<G> {

    /**
     * Repairs candidate and leaves explicit penalty integration to higher-level flow.
     *
     * @param candidate candidate genotype
     * @param representation genotype representation
     * @param problem optimization problem
     * @param rng random stream
     * @return repaired genotype
     */
    @Override
    public G enforce(G candidate, Representation<G> representation, Problem<G> problem, RngStream rng) {
        return representation.repair(candidate);
    }

    /**
     * Returns constraint-handling identifier.
     *
     * @return strategy identifier
     */
    @Override
    public String name() {
        return "penalty";
    }
}
