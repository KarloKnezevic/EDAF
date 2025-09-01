package com.knezevic.edaf.selection;

import com.knezevic.edaf.core.api.Individual;
import com.knezevic.edaf.core.api.Population;
import com.knezevic.edaf.core.api.Selection;
import com.knezevic.edaf.core.impl.SimplePopulation;

/**
 * (μ + λ) selection.
 * <p>
 * Selects the μ best individuals from the combined population of parents and offspring.
 *
 * @param <T> The type of individual.
 */
public class MuPlusLambdaSelection<T extends Individual> implements Selection<T> {

    private final int mu;

    public MuPlusLambdaSelection(int mu) {
        this.mu = mu;
    }

    @Override
    public Population<T> select(Population<T> population, int lambda) {
        if (population.getSize() == 0) {
            return new SimplePopulation<>();
        }

        population.sort();
        Population<T> newPopulation = new SimplePopulation<>();
        for (int i = 0; i < mu; i++) {
            newPopulation.add(population.getIndividual(i));
        }

        return newPopulation;
    }
}
