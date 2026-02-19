package com.knezevic.edaf.v3.core.api.defaults;

import com.knezevic.edaf.v3.core.api.ConstraintHandling;
import com.knezevic.edaf.v3.core.api.Problem;
import com.knezevic.edaf.v3.core.api.Representation;
import com.knezevic.edaf.v3.core.rng.RngStream;

/**
 * Constraint strategy using bounded rejection-resampling.
 */
public final class RejectionConstraintHandling<G> implements ConstraintHandling<G> {

    private final int maxRetries;

    public RejectionConstraintHandling(int maxRetries) {
        this.maxRetries = Math.max(1, maxRetries);
    }

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

    @Override
    public String name() {
        return "rejection";
    }
}
