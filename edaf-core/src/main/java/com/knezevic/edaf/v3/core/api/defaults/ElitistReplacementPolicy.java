package com.knezevic.edaf.v3.core.api.defaults;

import com.knezevic.edaf.v3.core.api.Individual;
import com.knezevic.edaf.v3.core.api.ObjectiveSense;
import com.knezevic.edaf.v3.core.api.Population;
import com.knezevic.edaf.v3.core.api.ReplacementPolicy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Replacement policy that keeps top elites from current population and fills remaining slots with offspring.
 */
public final class ElitistReplacementPolicy<G> implements ReplacementPolicy<G> {

    @Override
    public Population<G> replace(Population<G> current, List<Individual<G>> offspring, int elitism, ObjectiveSense sense) {
        List<Individual<G>> sortedCurrent = new ArrayList<>(current.asList());
        Comparator<Individual<G>> comparator = Comparator.comparingDouble(i -> i.fitness().scalar());
        if (sense == ObjectiveSense.MAXIMIZE) {
            comparator = comparator.reversed();
        }
        sortedCurrent.sort(comparator);

        List<Individual<G>> next = new ArrayList<>();
        int elites = Math.max(0, Math.min(elitism, sortedCurrent.size()));
        for (int i = 0; i < elites; i++) {
            next.add(sortedCurrent.get(i));
        }
        next.addAll(offspring);
        next.sort(comparator);

        int target = current.size();
        if (next.size() > target) {
            next = new ArrayList<>(next.subList(0, target));
        }
        Population<G> result = new Population<>(sense);
        result.addAll(next);
        return result;
    }

    @Override
    public String name() {
        return "elitist";
    }
}
