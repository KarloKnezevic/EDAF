/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.core.api;

import java.util.List;

/**
 * Optimization problem contract.
 *
 * @param <G> genotype value type.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public interface Problem<G> {

    /**
     * Returns the stable problem identifier used in logs, persistence, and reports.
     *
     * @return problem identifier
     */
    String name();

    /**
     * Returns the objective direction of the problem.
     *
     * @return objective sense
     */
    ObjectiveSense objectiveSense();

    /**
     * Evaluates one genotype and returns the computed fitness.
     *
     * @param genotype candidate solution to evaluate
     * @return evaluated fitness value
     */
    Fitness evaluate(G genotype);

    /**
     * Checks whether the genotype satisfies all hard feasibility constraints.
     *
     * @param genotype candidate solution to validate
     * @return true when genotype is feasible
     */
    default boolean feasible(G genotype) {
        return violations(genotype).isEmpty();
    }

    /**
     * Returns all constraint violation messages for diagnostics.
     *
     * @param genotype candidate solution to validate
     * @return list of violation messages
     */
    default List<String> violations(G genotype) {
        return List.of();
    }

    /**
     * Returns the number of optimization objectives exposed by the problem.
     *
     * @return objective count
     */
    default int objectiveCount() {
        return 1;
    }
}
