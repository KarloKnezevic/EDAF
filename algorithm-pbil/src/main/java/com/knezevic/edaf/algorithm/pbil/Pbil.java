package com.knezevic.edaf.algorithm.pbil;

import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.core.impl.SimplePopulation;
import com.knezevic.edaf.core.runtime.ExecutionContext;
import com.knezevic.edaf.core.runtime.SupportsExecutionContext;

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
public class Pbil<T extends Individual> implements Algorithm<T>, SupportsExecutionContext {

    private final Problem<T> problem;
    private final Statistics<T> statistics;
    private final TerminationCondition<T> terminationCondition;
    private final int populationSize;
    private final double learningRate;

    private T best;
    private int generation;
    private Population<T> population;
    private ProgressListener listener;
    private ExecutionContext context;

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
            // 2.1. Elitism: preserve the best individual from previous population (if exists)
            T bestFromPrevious = (population != null && population.getSize() > 0) 
                ? (T) population.getBest().copy() 
                : null;
            
            // 2.2. Sample a population
            Population<T> sampledPopulation = statistics.sample(populationSize);
            population = new SimplePopulation<>(problem.getOptimizationType());
            for (T individual : sampledPopulation) {
                population.add(individual);
            }

            // 2.3. Evaluate the population
            long e0 = System.nanoTime();
            for (T individual : population) {
                problem.evaluate(individual);
            }
            long e1 = System.nanoTime();
            if (context != null && context.getEvents() != null) {
                context.getEvents().publish(new com.knezevic.edaf.core.runtime.EvaluationCompleted("pbil", generation, population.getSize(), e1 - e0));
            }
            population.sort();

            // 2.4. Ensure best individual is preserved (elitism)
            // Replace worst if best from previous generation is better than current best
            if (bestFromPrevious != null) {
                T currentBest = population.getBest();
                if (isFirstBetter(bestFromPrevious, currentBest)) {
                    // Best from previous generation is better, replace worst with it
                    population.remove(population.getWorst());
                    population.add((T) bestFromPrevious.copy());
                    population.sort();
                }
            }

            // 2.5. Get the best individual
            T currentBest = population.getBest();

            // 2.6. Update the probability vector
            statistics.update(currentBest, learningRate);

            // 2.7. Update the best-so-far individual
            if (isFirstBetter(currentBest, best)) {
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

    @Override
    public void setExecutionContext(ExecutionContext context) {
        this.context = context;
    }

    private boolean isFirstBetter(Individual first, Individual second) {
        if (second == null) {
            return true;
        }
        if (problem.getOptimizationType() == OptimizationType.min) {
            return first.getFitness() < second.getFitness();
        } else {
            return first.getFitness() > second.getFitness();
        }
    }
}
