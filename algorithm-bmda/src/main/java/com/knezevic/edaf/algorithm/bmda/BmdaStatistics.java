package com.knezevic.edaf.algorithm.bmda;

import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.core.impl.SimplePopulation;
import com.knezevic.edaf.genotype.binary.BinaryIndividual;

import java.util.Random;

/**
 * Implements the statistics component for the Bivariate Marginal Distribution Algorithm (BMDA).
 *
 * @param <T> The type of individual in the population.
 */
public class BmdaStatistics<T extends Individual<byte[]>> implements Statistics<T> {

    private BivariateDistribution[][] distributions;
    private int chromosomeLength;
    private final Random random;

    public BmdaStatistics() {
        this.random = new Random();
    }

    public BmdaStatistics(Random random) {
        this.random = random;
    }

    @Override
    public void estimate(Population<T> population) {
        if (population.getSize() == 0) {
            return;
        }

        // Assuming all individuals have the same length.
        T first = population.iterator().next();
        chromosomeLength = first.getGenotype().length;
        distributions = new BivariateDistribution[chromosomeLength][chromosomeLength];

        // Initialize the distributions.
        for (int i = 0; i < chromosomeLength; i++) {
            for (int j = i + 1; j < chromosomeLength; j++) {
                distributions[i][j] = new BivariateDistribution();
            }
        }

        // Update the distributions with the individuals from the population.
        for (T individual : population) {
            updateDistributions(individual);
        }

        // Normalize the distributions.
        for (int i = 0; i < chromosomeLength; i++) {
            for (int j = i + 1; j < chromosomeLength; j++) {
                distributions[i][j].normalize(population.getSize());
            }
        }
    }

    private void updateDistributions(T individual) {
        byte[] chromosome = individual.getGenotype();
        for (int i = 0; i < chromosomeLength; i++) {
            for (int j = i + 1; j < chromosomeLength; j++) {
                distributions[i][j].update(chromosome[i], chromosome[j]);
            }
        }
    }

    @Override
    public void update(T individual, double learningRate) {
        // TODO: Implement the update of the model.
    }

    @Override
    public Population<T> sample(int size) {
        // TODO: This is a simplified sampling method and may not be correct.
        // A more sophisticated approach would be to build a dependency graph
        // (e.g., a Chow-Liu tree) and sample from it.

        Population<T> newPopulation = new SimplePopulation<>(OptimizationType.min);
        for (int i = 0; i < size; i++) {
            byte[] chromosome = new byte[chromosomeLength];
            // Generate the first bit based on its marginal probability.
            double p1 = 0;
            for (int j = 1; j < chromosomeLength; j++) {
                p1 += distributions[0][j].getProbability(1, 0) + distributions[0][j].getProbability(1, 1);
            }
            p1 /= (chromosomeLength - 1);
            chromosome[0] = (byte) (random.nextDouble() < p1 ? 1 : 0);

            // Generate subsequent bits based on conditional probabilities.
            for (int j = 1; j < chromosomeLength; j++) {
                int prevBit = chromosome[j - 1];
                double p_cond = distributions[j - 1][j].getProbability(prevBit, 1)
                        / (distributions[j - 1][j].getProbability(prevBit, 0) + distributions[j - 1][j].getProbability(prevBit, 1));
                chromosome[j] = (byte) (random.nextDouble() < p_cond ? 1 : 0);
            }

            T newIndividual = (T) new BinaryIndividual(chromosome);
            newPopulation.add(newIndividual);
        }
        return newPopulation;
    }
}
