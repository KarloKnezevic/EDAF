package com.knezevic.edaf.v3.core.api.defaults;

import com.knezevic.edaf.v3.core.api.ConstraintHandling;
import com.knezevic.edaf.v3.core.api.Problem;
import com.knezevic.edaf.v3.core.api.Representation;
import com.knezevic.edaf.v3.core.rng.RngStream;

/**
 * Default constraint strategy that repairs candidates through representation-level repair only.
 */
public final class IdentityConstraintHandling<G> implements ConstraintHandling<G> {

    @Override
    public G enforce(G candidate, Representation<G> representation, Problem<G> problem, RngStream rng) {
        return representation.repair(candidate);
    }

    @Override
    public String name() {
        return "identity";
    }
}
