package com.knezevic.edaf.core.impl;

import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.core.runtime.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Base class for all optimization algorithms in the EDAF framework.
 * <p>
 * Provides common infrastructure: fitness comparison, event publishing,
 * parallel population evaluation, best-individual tracking, and generation counting.
 * Subclasses implement {@link #run()} and {@link #getPopulation()}.
 * </p>
 *
 * @param <T> The type of individual in the population.
 */
public abstract class AbstractAlgorithm<T extends Individual> implements Algorithm<T>, SupportsExecutionContext {

    protected final Problem<T> problem;
    protected final String algorithmId;

    private T best;
    private int generation;
    private ProgressListener listener;
    private ExecutionContext context;

    protected AbstractAlgorithm(Problem<T> problem, String algorithmId) {
        this.problem = problem;
        this.algorithmId = algorithmId;
    }

    // ── Best individual management ──────────────────────────────────────

    @Override
    public final T getBest() {
        return best;
    }

    @SuppressWarnings("unchecked")
    protected final void setBest(T individual) {
        this.best = (T) individual.copy();
    }

    /**
     * Updates the best individual if the candidate is better than the current best.
     *
     * @return true if the best was updated.
     */
    protected final boolean updateBestIfBetter(T candidate) {
        if (isFirstBetter(candidate, best)) {
            setBest(candidate);
            return true;
        }
        return false;
    }

    // ── Generation management ───────────────────────────────────────────

    @Override
    public final int getGeneration() {
        return generation;
    }

    protected final void setGeneration(int generation) {
        this.generation = generation;
    }

    protected final void incrementGeneration() {
        this.generation++;
    }

    // ── Listener ────────────────────────────────────────────────────────

    @Override
    public final void setProgressListener(ProgressListener listener) {
        this.listener = listener;
    }

    protected final void notifyListener() {
        if (listener != null) {
            notifyListener(getPopulation());
        }
    }

    protected final void notifyListener(Population<T> population) {
        if (listener != null) {
            listener.onGenerationDone(generation, best, population);
        }
    }

    // ── Execution context ───────────────────────────────────────────────

    @Override
    public void setExecutionContext(ExecutionContext context) {
        this.context = context;
    }

    protected final ExecutionContext getContext() {
        return context;
    }

    // ── Fitness comparison ──────────────────────────────────────────────

    protected final boolean isFirstBetter(Individual first, Individual second) {
        if (second == null) return true;
        if (problem.getOptimizationType() == OptimizationType.min) {
            return first.getFitness() < second.getFitness();
        } else {
            return first.getFitness() > second.getFitness();
        }
    }

    // ── Event publishing ────────────────────────────────────────────────

    protected final void publishEvent(Object event) {
        if (context != null && context.getEvents() != null) {
            context.getEvents().publish(event);
        }
    }

    protected final void publishAlgorithmStarted() {
        publishEvent(new AlgorithmStarted(algorithmId));
    }

    protected final void publishAlgorithmTerminated() {
        publishEvent(new AlgorithmTerminated(algorithmId, generation));
    }

    protected final void publishEvaluationCompleted(int gen, int count, long nanos) {
        publishEvent(new EvaluationCompleted(algorithmId, gen, count, nanos));
    }

    /**
     * Publishes a GenerationCompleted event with population statistics.
     * Uses the population returned by {@link #getPopulation()}.
     */
    protected final void publishGenerationCompleted() {
        publishGenerationCompleted(getPopulation());
    }

    /**
     * Publishes a GenerationCompleted event with statistics from the given population.
     */
    protected final void publishGenerationCompleted(Population<T> population) {
        if (context != null && context.getEvents() != null && population != null && population.getSize() > 0) {
            PopulationStatistics.Statistics stats = PopulationStatistics.calculate(population);
            context.getEvents().publish(new GenerationCompleted(
                    algorithmId, generation, population.getBest(),
                    stats.best(), stats.worst(), stats.avg(), stats.std()));
        }
    }

    /**
     * Publishes a GenerationCompleted event with explicit fitness values (no population stats).
     */
    protected final void publishGenerationCompleted(double bestFitness, double worstFitness,
                                                     double avgFitness, double stdFitness) {
        publishEvent(new GenerationCompleted(algorithmId, generation, best,
                bestFitness, worstFitness, avgFitness, stdFitness));
    }

    // ── Parallel population evaluation ──────────────────────────────────

    /**
     * Evaluates all individuals in the population in parallel using the context executor
     * or a default thread pool.
     */
    protected final void evaluatePopulation(Population<T> population) {
        evaluatePopulationFrom(population, 0);
    }

    /**
     * Evaluates individuals in the population starting from the given index.
     * Useful for skipping elite individuals that are already evaluated.
     */
    protected final void evaluatePopulationFrom(Population<T> population, int fromIndex) {
        ExecutorService executor = context != null && context.getExecutor() != null
                ? context.getExecutor()
                : Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<Callable<Void>> tasks = new ArrayList<>();
        for (int i = fromIndex; i < population.getSize(); i++) {
            final T individual = population.getIndividual(i);
            tasks.add(() -> {
                problem.evaluate(individual);
                return null;
            });
        }
        try {
            executor.invokeAll(tasks);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        if (context == null) {
            executor.shutdown();
        }
    }

    /**
     * Evaluates all individuals, publishes an EvaluationCompleted event, and returns elapsed nanos.
     */
    protected final long evaluateAndPublish(Population<T> population, int gen) {
        long t0 = System.nanoTime();
        evaluatePopulation(population);
        long t1 = System.nanoTime();
        publishEvaluationCompleted(gen, population.getSize(), t1 - t0);
        return t1 - t0;
    }

    // ── Elitism helper ──────────────────────────────────────────────────

    /**
     * Applies elitism: if bestFromPrevious is better than current population best,
     * replaces the worst individual with bestFromPrevious.
     */
    @SuppressWarnings("unchecked")
    protected final void applyElitism(Population<T> population, T bestFromPrevious) {
        if (bestFromPrevious == null) return;
        T currentBest = population.getBest();
        if (isFirstBetter(bestFromPrevious, currentBest)) {
            population.remove(population.getWorst());
            population.add((T) bestFromPrevious.copy());
            population.sort();
        }
    }
}
