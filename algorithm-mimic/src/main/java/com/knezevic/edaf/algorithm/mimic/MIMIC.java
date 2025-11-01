package com.knezevic.edaf.algorithm.mimic;

import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.genotype.binary.BinaryIndividual;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.knezevic.edaf.core.runtime.ExecutionContext;
import com.knezevic.edaf.core.runtime.SupportsExecutionContext;

/**
 * The MIMIC algorithm.
 */
public class MIMIC implements Algorithm<BinaryIndividual>, SupportsExecutionContext {

    private final Problem<BinaryIndividual> problem;
    private final Population<BinaryIndividual> population;
    private final Selection<BinaryIndividual> selection;
    private final Statistics<BinaryIndividual> statistics;
    private final TerminationCondition<BinaryIndividual> terminationCondition;
    private final int selectionSize;

    private BinaryIndividual best;
    private int generation;
    private ProgressListener listener;
    private ExecutionContext context;

    public MIMIC(Problem<BinaryIndividual> problem, Population<BinaryIndividual> population,
                 Selection<BinaryIndividual> selection, Statistics<BinaryIndividual> statistics,
                 TerminationCondition<BinaryIndividual> terminationCondition, int selectionSize) {
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
            context.getEvents().publish(new com.knezevic.edaf.core.runtime.EvaluationCompleted("mimic", 0, population.getSize(), t1 - t0));
        }
        population.sort();
        best = (BinaryIndividual) population.getBest().copy();
        generation = 0;

        // 2. Run generations
        while (!terminationCondition.shouldTerminate(this)) {
            // 2.1. Select best individuals
            Population<BinaryIndividual> selected = selection.select(population, selectionSize);

            // 2.2. Build probabilistic model
            statistics.estimate(selected);

            // 2.3. Sample new individuals
            Population<BinaryIndividual> newPopulation = statistics.sample(population.getSize());

            // 2.4. Evaluate new individuals
            long e0 = System.nanoTime();
            evaluatePopulation(newPopulation);
            long e1 = System.nanoTime();
            if (context != null && context.getEvents() != null) {
                context.getEvents().publish(new com.knezevic.edaf.core.runtime.EvaluationCompleted("mimic", generation, newPopulation.getSize(), e1 - e0));
            }

            // 2.5. Elitism: preserve the best individual from current population
            BinaryIndividual bestFromCurrent = population.getBest();
            
            // 2.6. Replace old population
            population.clear();
            for (int i = 0; i < newPopulation.getSize(); i++) {
                population.add(newPopulation.getIndividual(i));
            }
            population.sort();

            // 2.7. Ensure best individual is preserved (elitism)
            // Replace worst if best from previous generation is better than current best
            BinaryIndividual currentBest = population.getBest();
            if (bestFromCurrent.getFitness() < currentBest.getFitness()) {
                // Best from previous generation is better, replace worst with it
                population.remove(population.getWorst());
                population.add((BinaryIndividual) bestFromCurrent.copy());
                population.sort();
                currentBest = population.getBest();
            }

            // 2.8. Update best individual
            if (currentBest.getFitness() < best.getFitness()) {
                best = (BinaryIndividual) currentBest.copy();
            }

            generation++;
            if (listener != null) {
                listener.onGenerationDone(generation, population.getBest(), population);
            }
        }
    }

    private void evaluatePopulation(Population<BinaryIndividual> population) {
        ExecutorService executor = context != null && context.getExecutor() != null
                ? context.getExecutor()
                : Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<Callable<Void>> tasks = new ArrayList<>();
        for (int i = 0; i < population.getSize(); i++) {
            final BinaryIndividual individual = population.getIndividual(i);
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
    public BinaryIndividual getBest() {
        return best;
    }

    @Override
    public int getGeneration() {
        return generation;
    }

    @Override
    public Population<BinaryIndividual> getPopulation() {
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
}
