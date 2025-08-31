package com.knezevic.edaf.algorithm.bmda;

import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.core.impl.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The Bivariate Marginal Distribution Algorithm (BMDA).
 *
 * @param <T> The type of individual in the population.
 */
public class Bmda<T extends Individual> implements Algorithm<T> {

    private final Problem<T> problem;
    private final Population<T> population;
    private final Selection<T> selection;
    private final Statistics<T> statistics;
    private final TerminationCondition<T> terminationCondition;
    private final int selectionSize;

    private T best;
    private int generation;
    private ProgressListener listener;
    private final ExecutorService executor;

    public Bmda(Problem<T> problem, Population<T> population, Selection<T> selection,
                Statistics<T> statistics, TerminationCondition<T> terminationCondition,
                int selectionSize) {
        this.problem = problem;
        this.population = population;
        this.selection = selection;
        this.statistics = statistics;
        this.terminationCondition = terminationCondition;
        this.selectionSize = selectionSize;
        this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    @Override
    public void run() {
        // 1. Initialize population
        evaluatePopulation(population);
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
            evaluatePopulation(newPopulation);

            // 2.5. Replace old population
            population.clear();
            for (T individual : newPopulation) {
                population.add(individual);
            }
            population.sort();

            // 2.6. Update best individual
            T currentBest = population.getBest();
            if (currentBest.getFitness() < best.getFitness()) {
                best = (T) currentBest.copy();
            }

            generation++;
            if (listener != null) {
                listener.onGenerationDone(generation, population.getBest(), population);
            }
        }
        executor.shutdown();
    }

    private void evaluatePopulation(Population<T> population) {
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
            e.printStackTrace();
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
