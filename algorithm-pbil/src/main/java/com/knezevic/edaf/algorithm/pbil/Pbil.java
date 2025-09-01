package com.knezevic.edaf.algorithm.pbil;

import com.knezevic.edaf.core.api.*;

/**
 * Population-Based Incremental Learning (PBIL).
 * <p>
 * PBIL is an optimization algorithm that combines aspects of genetic algorithms and competitive learning.
 * It evolves a probability vector, which is used to generate populations of candidate solutions.
 * </p>
 * <p>
 * The algorithm works as follows:
 * <ol>
 *     <li>Initialize a probability vector. For binary problems, this is a vector of probabilities for each bit being 1.
 *         For continuous problems, this can be a vector of means and standard deviations for a normal distribution.</li>
 *     <li>Generate a population of individuals by sampling from the probability distribution defined by the probability vector.</li>
 *     <li>Evaluate the fitness of each individual in the population.</li>
 *     <li>Identify the best individual in the population.</li>
 *     <li>Update the probability vector by shifting it towards the best individual. The amount of shift is controlled by a learning rate.</li>
 *     <li>Repeat from step 2 until a termination condition is met.</li>
 * </ol>
 * </p>
 * <p>
 * This implementation is generic and can work with different types of individuals and statistics.
 * For binary problems, use {@link com.knezevic.edaf.statistics.discrete.BitwiseDistribution}.
 * For continuous problems, use {@link com.knezevic.edaf.statistics.continuous.NormalDistribution}.
 * </p>
 *
 * @param <T> The type of individual in the population.
 */
public class Pbil<T extends Individual> implements Algorithm<T> {

    private final Problem<T> problem;
    private final Statistics<T> statistics;
    private final TerminationCondition<T> terminationCondition;
    private final int populationSize;
    private final double learningRate;

    private T best;
    private int generation;
    private Population<T> population;
    private ProgressListener listener;

    public Pbil(Problem<T> problem, Statistics<T> statistics,
                TerminationCondition<T> terminationCondition, int populationSize,
                double learningRate) {
        this.problem = problem;
        this.statistics = statistics;
        this.terminationCondition = terminationCondition;
        this.populationSize = populationSize;
        this.learningRate = learningRate;
    }

    @Override
    public void run() {
        // 1. Initialize probability vector (done in statistics impl)
        generation = 0;

        // 2. Run generations
        while (!terminationCondition.shouldTerminate(this)) {
            // 2.1. Sample a population
            population = statistics.sample(populationSize);

            // 2.2. Evaluate the population
            for (T individual : population) {
                problem.evaluate(individual);
            }
            population.sort();

            // 2.3. Get the best individual
            T currentBest = population.getBest();

            // 2.4. Update the probability vector
            statistics.update(currentBest, learningRate);

            // 2.5. Update the best-so-far individual
            if (best == null || currentBest.getFitness() < best.getFitness()) {
                best = (T) currentBest.copy();
            }

            generation++;
            if (listener != null) {
                listener.onGenerationDone(generation, population.getBest(), population);
            }
        }
    }

    @Override
    public T getBest() {
        return best;
    }

    @Override
    public int getGeneration() {
        return generation;
    }

    @Override
    public Population<T> getPopulation() {
        return population;
    }

    @Override
    public void setProgressListener(ProgressListener listener) {
        this.listener = listener;
    }
}
