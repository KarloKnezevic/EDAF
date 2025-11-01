package com.knezevic.edaf.algorithm.cem;

import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.core.impl.SimplePopulation;
import com.knezevic.edaf.core.runtime.AlgorithmStarted;
import com.knezevic.edaf.core.runtime.AlgorithmTerminated;
import com.knezevic.edaf.core.runtime.EvaluationCompleted;
import com.knezevic.edaf.core.runtime.GenerationCompleted;
import com.knezevic.edaf.core.runtime.PopulationStatistics;
import com.knezevic.edaf.core.runtime.ExecutionContext;
import com.knezevic.edaf.core.runtime.SupportsExecutionContext;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
public class CEM<T extends Individual> implements Algorithm<T>, SupportsExecutionContext {

    private final Problem<T> problem;
    private final Statistics<T> statistics;
    private final TerminationCondition<T> terminationCondition;
    private final int batchSize;
    private final double eliteFraction;
    private final double learningRate;

    private T best;
    private int generation;
    private Population<T> population;
    private ProgressListener listener;
    private ExecutionContext context;

    public CEM(Problem<T> problem, Statistics<T> statistics,
               TerminationCondition<T> terminationCondition, int batchSize,
               double eliteFraction, double learningRate) {
        this.problem = problem;
        this.statistics = statistics;
        this.terminationCondition = terminationCondition;
        this.batchSize = batchSize;
        this.eliteFraction = eliteFraction;
        this.learningRate = learningRate;
    }

    @Override
    public void run() {
        if (context != null && context.getEvents() != null) {
            context.getEvents().publish(new AlgorithmStarted("cem"));
        }
        
        generation = 0;

        // Main CEM loop
        while (!terminationCondition.shouldTerminate(this)) {
            // 1. Elitism: preserve the best individual from previous population (if exists)
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
            long e0 = System.nanoTime();
            evaluatePopulation(population);
            long e1 = System.nanoTime();
            if (context != null && context.getEvents() != null) {
                context.getEvents().publish(new EvaluationCompleted("cem", generation, population.getSize(), e1 - e0));
            }
            
            population.sort();

            // 4. Ensure best individual is preserved (elitism)
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

            // 5. Select elite solutions
            int eliteSize = Math.max(1, (int) (batchSize * eliteFraction));
            Population<T> elite = selectElite(population, eliteSize);

            // 6. Update distribution parameters based on elite solutions
            // This minimizes cross-entropy between empirical elite distribution and parametric distribution
            statistics.estimate(elite);

            // 7. Update best individual
            T currentBest = population.getBest();
            if (isFirstBetter(currentBest, best)) {
                best = (T) currentBest.copy();
            }

            generation++;
            if (listener != null) {
                listener.onGenerationDone(generation, population.getBest(), population);
            }
            if (context != null && context.getEvents() != null) {
                PopulationStatistics.Statistics stats = PopulationStatistics.calculate(population);
                context.getEvents().publish(new GenerationCompleted("cem", generation, population.getBest(),
                    stats.best(), stats.worst(), stats.avg(), stats.std()));
            }
        }
        
        if (context != null && context.getEvents() != null) {
            context.getEvents().publish(new AlgorithmTerminated("cem", generation));
        }
    }

    private Population<T> selectElite(Population<T> population, int eliteSize) {
        Population<T> elite = new SimplePopulation<>(problem.getOptimizationType());
        for (int i = 0; i < Math.min(eliteSize, population.getSize()); i++) {
            elite.add((T) population.getIndividual(i).copy());
        }
        return elite;
    }

    private void evaluatePopulation(Population<T> population) {
        ExecutorService executor = context != null && context.getExecutor() != null
                ? context.getExecutor()
                : Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<Callable<Void>> tasks = new ArrayList<>();
        for (T individual : population) {
            tasks.add(() -> {
                problem.evaluate(individual);
                return null;
            });
        }
        try {
            executor.invokeAll(tasks);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Evaluation interrupted", e);
        }
        if (context == null) {
            executor.shutdown();
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

