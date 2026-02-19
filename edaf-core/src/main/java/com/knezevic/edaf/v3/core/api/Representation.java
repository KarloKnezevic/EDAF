package com.knezevic.edaf.v3.core.api;

import com.knezevic.edaf.v3.core.rng.RngStream;

/**
 * Domain-specific genotype representation.
 *
 * @param <G> genotype value type.
 */
public interface Representation<G> {

    /**
     * Representation identifier used in configuration and logs.
     */
    String type();

    /**
     * Creates a random feasible genotype.
     */
    G random(RngStream rng);

    /**
     * Returns true if the genotype is valid in this domain.
     */
    boolean isValid(G genotype);

    /**
     * Repairs a genotype into the feasible domain.
     */
    G repair(G genotype);

    /**
     * Provides a stable summary for logs and reports.
     */
    String summarize(G genotype);
}
