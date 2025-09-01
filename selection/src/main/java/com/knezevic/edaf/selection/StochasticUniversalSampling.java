package com.knezevic.edaf.selection;

import com.knezevic.edaf.core.api.Individual;
import com.knezevic.edaf.core.api.Population;
import com.knezevic.edaf.core.api.Selection;
import com.knezevic.edaf.core.impl.SimplePopulation;

import java.util.Random;

/**
 * Stochastic Universal Sampling.
 * <p>
 * This selection method assumes that all fitness values are non-negative.
 *
 * @param <T> The type of individual.
 */
public class StochasticUniversalSampling<T extends Individual> implements Selection<T> {

    private final Random random;

    public StochasticUniversalSampling(Random random) {
        this.random = random;
    }

    @Override
    public Population<T> select(Population<T> population, int size) {
        if (population.getSize() == 0) {
            return new SimplePopulation<>();
        }

        double totalFitness = 0;
        for (T individual : population) {
            totalFitness += individual.getFitness();
        }

        Population<T> newPopulation = new SimplePopulation<>();
        double step = totalFitness / size;
        double start = random.nextDouble() * step;
        double currentSum = 0;
        int i = 0;
        for (T individual : population) {
            currentSum += individual.getFitness();
            while (start < currentSum) {
                newPopulation.add(individual);
                start += step;
            }
        }

        return newPopulation;
    }
}
