package com.knezevic.edaf.core.impl;

import com.knezevic.edaf.core.api.Individual;
import com.knezevic.edaf.core.api.Population;
import com.knezevic.edaf.core.api.Selection;

import java.util.Random;

/**
 * Roulette wheel selection.
 * <p>
 * This selection method assumes that all fitness values are non-negative.
 *
 * @param <T> The type of individual.
 */
public class RouletteWheelSelection<T extends Individual> implements Selection<T> {

    private final Random random;

    public RouletteWheelSelection(Random random) {
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
        for (int i = 0; i < size; i++) {
            newPopulation.add(selectOne(population, totalFitness));
        }

        return newPopulation;
    }

    private T selectOne(Population<T> population, double totalFitness) {
        double slice = random.nextDouble() * totalFitness;
        double currentSum = 0;
        for (T individual : population) {
            currentSum += individual.getFitness();
            if (currentSum >= slice) {
                return individual;
            }
        }

        // Should not happen with positive fitness values, but as a fallback
        return population.getIndividual(population.getSize() - 1);
    }
}
