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
 * Constraint strategy using bounded rejection-resampling.
 *
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class RejectionConstraintHandling<G> implements ConstraintHandling<G> {

    private final int maxRetries;

    /**
     * Creates bounded rejection-resampling constraint handler.
     *
     * @param maxRetries maximum number of rejection retries
     */
    public RejectionConstraintHandling(int maxRetries) {
        this.maxRetries = Math.max(1, maxRetries);
    }

    /**
     * Enforces constraints via repair and bounded rejection resampling.
     *
     * @param candidate candidate genotype
     * @param representation genotype representation
     * @param problem optimization problem
     * @param rng random stream
     * @return feasible or repaired genotype
     */
    @Override
    public G enforce(G candidate, Representation<G> representation, Problem<G> problem, RngStream rng) {
        G value = candidate;
        for (int i = 0; i < maxRetries; i++) {
            G repaired = representation.repair(value);
            if (problem == null || problem.feasible(repaired)) {
                return repaired;
            }
            value = representation.random(rng);
        }
        return representation.repair(value);
    }

    /**
     * Returns constraint-handling identifier.
     *
     * @return strategy identifier
     */
    @Override
    public String name() {
        return "rejection";
    }
}
