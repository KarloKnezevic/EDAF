package com.knezevic.edaf.algorithm.cem;

import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.core.impl.AbstractAlgorithm;
import com.knezevic.edaf.core.impl.SimplePopulation;

/**
 * Cross-Entropy Method (CEM).
 *
 * CEM is a stochastic optimization technique that minimizes cross-entropy between
 * a target distribution and a parametric distribution. The algorithm:
 *
 * <ol>
 *     <li>Samples candidate solutions from parametric distribution</li>
 *     <li>Evaluates all candidates</li>
 *     <li>Selects elite solutions (top fraction, e.g., 0.1-0.2)</li>
 *     <li>Updates distribution parameters based on elite solutions</li>
 *     <li>Repeats until convergence</li>
 * </ol>
 *
 * <p>
 * CEM supports both continuous (Gaussian) and discrete (Bernoulli) optimization problems.
 * </p>
 *
 * @param <T> The type of individual in the population.
 */
public class CEM<T extends Individual> extends AbstractAlgorithm<T> {

    private final Statistics<T> statistics;
    private final TerminationCondition<T> terminationCondition;
    private final int batchSize;
    private final double eliteFraction;
    private final double learningRate;

    private Population<T> population;

    public CEM(Problem<T> problem, Statistics<T> statistics,
               TerminationCondition<T> terminationCondition, int batchSize,
               double eliteFraction, double learningRate) {
        super(problem, "cem");
        this.statistics = statistics;
        this.terminationCondition = terminationCondition;
        this.batchSize = batchSize;
        this.eliteFraction = eliteFraction;
        this.learningRate = learningRate;
    }

    @Override
    public void run() {
        publishAlgorithmStarted();

        setGeneration(0);

        // Main CEM loop
        while (!terminationCondition.shouldTerminate(this)) {
            // 1. Elitism: preserve the best individual from previous population (if exists)
            @SuppressWarnings("unchecked")
            T bestFromPrevious = (population != null && population.getSize() > 0)
                ? (T) population.getBest().copy()
                : null;

            // 2. Sample candidate solutions
            Population<T> candidates = statistics.sample(batchSize);
            population = new SimplePopulation<>(problem.getOptimizationType());
            for (T individual : candidates) {
                population.add(individual);
            }

            // 3. Evaluate all candidates
            evaluateAndPublish(population, getGeneration());

            population.sort();

            // 4. Ensure best individual is preserved (elitism)
            applyElitism(population, bestFromPrevious);

            // 5. Select elite solutions
            int eliteSize = Math.max(1, (int) (batchSize * eliteFraction));
            Population<T> elite = selectElite(population, eliteSize);

            // 6. Update distribution parameters based on elite solutions
            // This minimizes cross-entropy between empirical elite distribution and parametric distribution
            statistics.estimate(elite);

            // 7. Update best individual
            T currentBest = population.getBest();
            updateBestIfBetter(currentBest);

            incrementGeneration();
            notifyListener();
            publishGenerationCompleted();
        }

        publishAlgorithmTerminated();
    }

    @SuppressWarnings("unchecked")
    private Population<T> selectElite(Population<T> population, int eliteSize) {
        Population<T> elite = new SimplePopulation<>(problem.getOptimizationType());
        for (int i = 0; i < Math.min(eliteSize, population.getSize()); i++) {
            elite.add((T) population.getIndividual(i).copy());
        }
        return elite;
    }

    @Override
    public Population<T> getPopulation() {
        return population;
    }
}
