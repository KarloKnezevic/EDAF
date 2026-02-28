/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.core.api;

import com.knezevic.edaf.v3.core.rng.RngStream;

import java.util.List;

/**
 * Strategy for selecting individuals used for model fitting or parent selection.
 *
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public interface SelectionPolicy<G> {

    /**
     * Selects a subset from the current population.
     *
     * @param population current population
     * @param count requested number of selected individuals
     * @param rng random stream used by stochastic selection policies
     * @return selected individuals
     */
    List<Individual<G>> select(Population<G> population, int count, RngStream rng);

    /**
     * Returns the policy identifier used in logs and reports.
     *
     * @return selection policy identifier
     */
    String name();
}
