package com.knezevic.edaf.algorithm.bmda;

import com.knezevic.edaf.core.api.Algorithm;
import com.knezevic.edaf.core.api.Individual;
import com.knezevic.edaf.core.api.Population;
import com.knezevic.edaf.core.api.Problem;
import com.knezevic.edaf.core.api.ProgressListener;
import com.knezevic.edaf.core.api.OptimizationType;
import com.knezevic.edaf.core.api.Selection;
import com.knezevic.edaf.core.api.Statistics;
import com.knezevic.edaf.core.api.TerminationCondition;
import com.knezevic.edaf.core.impl.SimplePopulation;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.knezevic.edaf.core.runtime.ExecutionContext;
import com.knezevic.edaf.core.runtime.SupportsExecutionContext;

/**
 * The Bivariate Marginal Distribution Algorithm (BMDA).
 *
 * @param <T> The type of individual in the population.
 */
public class Bmda<T extends Individual> implements Algorithm<T>, SupportsExecutionContext {

    private final Problem<T> problem;
    private final Population<T> population;
    private final Selection<T> selection;
    private final Statistics<T> statistics;
    private final TerminationCondition<T> terminationCondition;
    private final int selectionSize;

    private T best;
    private int generation;
    private ProgressListener listener;
    private ExecutionContext context;

    public Bmda(Problem<T> problem, Population<T> population, Selection<T> selection,
                Statistics<T> statistics, TerminationCondition<T> terminationCondition,
                int selectionSize) {
        this.problem = problem;
        this.population = population;
        this.selection = selection;
        this.statistics = statistics;
        this.terminationCondition = terminationCondition;
        this.selectionSize = selectionSize;
    }

    @Override
    public void run() {
        // 1. Initialize population
        long t0 = System.nanoTime();
        evaluatePopulation(population);
        long t1 = System.nanoTime();
        if (context != null && context.getEvents() != null) {
            context.getEvents().publish(new com.knezevic.edaf.core.runtime.EvaluationCompleted("bmda", 0, population.getSize(), t1 - t0));
        }
        population.sort();
        best = (T) population.getBest().copy();
        generation = 0;

        // 2. Run generations
        while (!terminationCondition.shouldTerminate(this)) {
            // 2.1. Select best individuals
            Population<T> selected = selection.select(population, selectionSize);

            // 2.2. Build probabilistic model
            statistics.estimate(selected);

            // 2.3. Sample new individuals
            Population<T> newPopulation = statistics.sample(population.getSize());

            // 2.4. Evaluate new individuals
            long e0 = System.nanoTime();
            evaluatePopulation(newPopulation);
            long e1 = System.nanoTime();
            if (context != null && context.getEvents() != null) {
                context.getEvents().publish(new com.knezevic.edaf.core.runtime.EvaluationCompleted("bmda", generation, newPopulation.getSize(), e1 - e0));
            }

            // 2.5. Elitism: preserve the best individual from current population
            T bestFromCurrent = population.getBest();
            
            // 2.6. Replace old population
            Population<T> correctlyTypedPopulation = new SimplePopulation<>(problem.getOptimizationType());
            for (T individual : newPopulation) {
                correctlyTypedPopulation.add(individual);
            }
            population.clear();
            for (T individual : correctlyTypedPopulation) {
                population.add(individual);
            }
            population.sort();

            // 2.7. Ensure best individual is preserved (elitism)
            // Replace worst if best from previous generation is better than current best
            T currentBest = population.getBest();
            if (isFirstBetter(bestFromCurrent, currentBest)) {
                // Best from previous generation is better, replace worst with it
                population.remove(population.getWorst());
                population.add((T) bestFromCurrent.copy());
                population.sort();
                currentBest = population.getBest();
            }

            // 2.8. Update best individual
            if (isFirstBetter(currentBest, best)) {
                best = (T) currentBest.copy();
            }

            generation++;
            if (listener != null) {
                listener.onGenerationDone(generation, population.getBest(), population);
            }
        }
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
        if (best == null) return true;
        if (problem.getOptimizationType() == OptimizationType.min) {
            return first.getFitness() < second.getFitness();
        } else {
            return first.getFitness() > second.getFitness();
        }
    }
}
