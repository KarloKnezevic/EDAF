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
 * K-tournament selection policy.
 *
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class TournamentSelectionPolicy<G> implements SelectionPolicy<G> {

    private final int k;

    /**
     * Creates tournament selection with minimum size of two.
     *
     * @param k tournament size
     */
    public TournamentSelectionPolicy(int k) {
        this.k = Math.max(2, k);
    }

    /**
     * Selects individuals with tournament competition.
     *
     * @param population source population
     * @param count number of individuals to select
     * @param rng random stream
     * @return selected individuals
     */
    @Override
    public List<Individual<G>> select(Population<G> population, int count, RngStream rng) {
        List<Individual<G>> selected = new ArrayList<>(count);
        Comparator<Individual<G>> comparator = Comparator.comparingDouble(i -> i.fitness().scalar());
        if (population.objectiveSense() == ObjectiveSense.MAXIMIZE) {
            comparator = comparator.reversed();
        }

        List<Individual<G>> all = population.asList();
        for (int i = 0; i < count; i++) {
            Individual<G> best = null;
            for (int j = 0; j < k; j++) {
                Individual<G> candidate = all.get(rng.nextInt(all.size()));
                if (best == null || comparator.compare(candidate, best) < 0) {
                    best = candidate;
                }
            }
            selected.add(best);
        }
        return selected;
    }

    /**
     * Returns selection-policy identifier.
     *
     * @return policy identifier
     */
    @Override
    public String name() {
        return "tournament";
    }
}
