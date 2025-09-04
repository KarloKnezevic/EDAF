package com.knezevic.edaf.selection;

import com.knezevic.edaf.core.api.Individual;
import com.knezevic.edaf.core.api.Population;
import com.knezevic.edaf.core.api.Selection;
import com.knezevic.edaf.core.impl.SimplePopulation;

/**
 * Truncated selection.
 * <p>
 * Selects the best `n` individuals from the population.
 *
 * @param <T> The type of individual.
 */
public class TruncatedSelection<T extends Individual> implements Selection<T> {

    @Override
    public Population<T> select(Population<T> population, int n) {
        if (population.getSize() == 0) {
            return new SimplePopulation<>(population.getOptimizationType());
        }

        population.sort();
        Population<T> newPopulation = new SimplePopulation<>(population.getOptimizationType());
        for (int i = 0; i < n; i++) {
            newPopulation.add(population.getIndividual(i));
        }

        return newPopulation;
    }
}
