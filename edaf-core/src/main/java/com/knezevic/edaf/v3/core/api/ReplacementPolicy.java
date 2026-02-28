/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.core.api;

import java.util.List;

/**
 * Strategy for producing next generation from current population and offspring.
 *
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public interface ReplacementPolicy<G> {

    /**
     * Builds the next generation from current population and offspring.
     *
     * @param current current generation population
     * @param offspring offspring produced in current iteration
     * @param elitism number of elite individuals preserved from current generation
     * @param sense objective optimization sense
     * @return next generation population
     */
    Population<G> replace(Population<G> current, List<Individual<G>> offspring, int elitism, ObjectiveSense sense);

    /**
     * Returns the policy identifier used in logs and reports.
     *
     * @return replacement policy identifier
     */
    String name();
}
