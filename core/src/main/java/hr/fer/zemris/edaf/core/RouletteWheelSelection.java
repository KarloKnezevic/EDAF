package hr.fer.zemris.edaf.core;

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
        if (population.isEmpty()) {
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
        return population.get(population.size() - 1);
    }
}
