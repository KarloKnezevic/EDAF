package com.knezevic.edaf.v3.core.api;

import java.util.List;

/**
 * Optimization problem contract.
 *
 * @param <G> genotype value type.
 */
public interface Problem<G> {

    /**
     * Problem identifier used for reporting and persistence.
     */
    String name();

    /**
     * Returns whether this problem is min or max.
     */
    ObjectiveSense objectiveSense();

    /**
     * Evaluates one genotype and returns its fitness.
     */
    Fitness evaluate(G genotype);

    /**
     * Returns true if the genotype satisfies hard constraints.
     */
    default boolean feasible(G genotype) {
        return violations(genotype).isEmpty();
    }

    /**
     * Returns validation/constraint violations for diagnostics.
     */
    default List<String> violations(G genotype) {
        return List.of();
    }

    /**
     * Number of objectives; defaults to one objective.
     */
    default int objectiveCount() {
        return 1;
    }
}
