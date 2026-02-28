/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.core.api.defaults;

import com.knezevic.edaf.v3.core.api.Individual;
import com.knezevic.edaf.v3.core.api.ObjectiveSense;
import com.knezevic.edaf.v3.core.api.Population;
import com.knezevic.edaf.v3.core.api.SelectionPolicy;
import com.knezevic.edaf.v3.core.rng.RngStream;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Selection policy that returns top-N individuals by fitness.
 *
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class TruncationSelectionPolicy<G> implements SelectionPolicy<G> {

    /**
     * Selects top individuals by scalar fitness ordering.
     *
     * @param population source population
     * @param count number of individuals to select
     * @param rng random stream
     * @return selected individuals
     */
    @Override
    public List<Individual<G>> select(Population<G> population, int count, RngStream rng) {
        List<Individual<G>> copy = new ArrayList<>(population.asList());
        Comparator<Individual<G>> comparator = Comparator.comparingDouble(i -> i.fitness().scalar());
        if (population.objectiveSense() == ObjectiveSense.MAXIMIZE) {
            comparator = comparator.reversed();
        }
        copy.sort(comparator);
        int limit = Math.max(1, Math.min(count, copy.size()));
        return new ArrayList<>(copy.subList(0, limit));
    }

    /**
     * Returns selection-policy identifier.
     *
     * @return policy identifier
     */
    @Override
    public String name() {
        return "truncation";
    }
}
