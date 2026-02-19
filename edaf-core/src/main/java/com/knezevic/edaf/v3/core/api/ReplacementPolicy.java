package com.knezevic.edaf.v3.core.api;

import java.util.List;

/**
 * Strategy for producing next generation from current population and offspring.
 */
public interface ReplacementPolicy<G> {

    /**
     * Returns the next generation population.
     */
    Population<G> replace(Population<G> current, List<Individual<G>> offspring, int elitism, ObjectiveSense sense);

    /**
     * Policy identifier used in logs and reports.
     */
    String name();
}
