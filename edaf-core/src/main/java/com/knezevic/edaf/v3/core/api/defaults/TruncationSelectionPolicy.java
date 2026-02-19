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
 */
public final class TruncationSelectionPolicy<G> implements SelectionPolicy<G> {

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

    @Override
    public String name() {
        return "truncation";
    }
}
