/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.core.api;

import com.knezevic.edaf.v3.core.rng.RngStream;

/**
 * Domain-specific genotype representation.
 *
 * @param <G> genotype value type.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public interface Representation<G> {

    /**
     * Returns representation identifier used in configuration and logs.
     *
     * @return representation identifier
     */
    String type();

    /**
     * Creates a random genotype candidate in the representation domain.
     *
     * @param rng random stream used for sampling
     * @return sampled genotype
     */
    G random(RngStream rng);

    /**
     * Checks whether the given genotype is valid in this representation.
     *
     * @param genotype genotype to validate
     * @return true when genotype is valid
     */
    boolean isValid(G genotype);

    /**
     * Repairs an invalid genotype into the feasible domain when possible.
     *
     * @param genotype genotype to repair
     * @return repaired genotype
     */
    G repair(G genotype);

    /**
     * Returns a stable and concise genotype summary for logs and reports.
     *
     * @param genotype genotype to summarize
     * @return human-readable summary
     */
    String summarize(G genotype);
}
