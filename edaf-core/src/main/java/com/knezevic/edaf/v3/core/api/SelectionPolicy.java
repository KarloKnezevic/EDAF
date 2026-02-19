package com.knezevic.edaf.v3.core.api;

import com.knezevic.edaf.v3.core.rng.RngStream;

import java.util.List;

/**
 * Strategy for selecting individuals used for model fitting or parent selection.
 */
public interface SelectionPolicy<G> {

    /**
     * Selects a subset from the current population.
     */
    List<Individual<G>> select(Population<G> population, int count, RngStream rng);

    /**
     * Policy identifier used in logs and reports.
     */
    String name();
}
