package hr.fer.zemris.edaf.core;

import java.util.Random;

/**
 * Roulette wheel selection.
 *
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
    public T select(Population<T> population) {
        double totalFitness = 0;
        for (int i = 0; i < population.getSize(); i++) {
            totalFitness += population.getIndividual(i).getFitness();
        }

        double slice = random.nextDouble() * totalFitness;
        double currentSum = 0;
        for (int i = 0; i < population.getSize(); i++) {
            currentSum += population.getIndividual(i).getFitness();
            if (currentSum >= slice) {
                return population.getIndividual(i);
            }
        }

        // Should not happen, but as a fallback, return the last individual
        return population.getIndividual(population.getSize() - 1);
    }
}
