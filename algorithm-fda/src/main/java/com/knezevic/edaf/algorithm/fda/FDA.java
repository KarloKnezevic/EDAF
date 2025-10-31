package com.knezevic.edaf.algorithm.fda;

import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.core.impl.SimplePopulation;
import com.knezevic.edaf.core.runtime.AlgorithmStarted;
import com.knezevic.edaf.core.runtime.AlgorithmTerminated;
import com.knezevic.edaf.core.runtime.EvaluationCompleted;
import com.knezevic.edaf.core.runtime.GenerationCompleted;
import com.knezevic.edaf.core.runtime.ExecutionContext;
import com.knezevic.edaf.core.runtime.SupportsExecutionContext;
import com.knezevic.edaf.genotype.binary.BinaryIndividual;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Factorized Distribution Algorithm (FDA).
 * 
 * FDA is a probabilistic model-building evolutionary algorithm that uses a Bayesian network
 * to capture dependencies between variables. The algorithm iteratively builds a factorized
 * probability distribution and samples new populations from it.
 * 
 * <p>
 * The algorithm works as follows:
 * <ol>
 *     <li>Initialize a population of random individuals and evaluate them.</li>
 *     <li>Select the best individuals from the current population.</li>
 *     <li>Build a Bayesian network model by learning the structure and parameters.</li>
 *     <li>Sample a new population from the learned model.</li>
 *     <li>Evaluate the new individuals and replace the old population.</li>
 *     <li>Repeat until termination condition is met.</li>
 * </ol>
 * </p>
 * 
 * <p>
 * FDA is particularly effective for problems with variable dependencies, as it captures
 * higher-order interactions compared to simpler EDAs like UMDA.
 * </p>
 *
 * @param <T> The type of individual in the population.
 */
public class FDA<T extends Individual<byte[]>> implements Algorithm<T>, SupportsExecutionContext {

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

    public FDA(Problem<T> problem, Population<T> population, Selection<T> selection,
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
        if (context != null && context.getEvents() != null) {
            context.getEvents().publish(new AlgorithmStarted("fda"));
        }
        
        long t0 = System.nanoTime();
        evaluatePopulation(population);
        long t1 = System.nanoTime();
        if (context != null && context.getEvents() != null) {
            context.getEvents().publish(new EvaluationCompleted("fda", 0, population.getSize(), t1 - t0));
        }
        
        population.sort();
        best = (T) population.getBest().copy();
        generation = 0;

        // 2. Run generations
        while (!terminationCondition.shouldTerminate(this)) {
            // 2.1. Select best individuals
            Population<T> selected = selection.select(population, selectionSize);

            // 2.2. Build probabilistic model (Bayesian network)
            statistics.estimate(selected);

            // 2.3. Sample new individuals from the model
            Population<T> newPopulation = statistics.sample(population.getSize());

            // 2.4. Evaluate new individuals
            long e0 = System.nanoTime();
            evaluatePopulation(newPopulation);
            long e1 = System.nanoTime();
            if (context != null && context.getEvents() != null) {
                context.getEvents().publish(new EvaluationCompleted("fda", generation, newPopulation.getSize(), e1 - e0));
            }

            // 2.5. Replace old population
            Population<T> correctlyTypedPopulation = new SimplePopulation<>(problem.getOptimizationType());
            for (T individual : newPopulation) {
                correctlyTypedPopulation.add(individual);
            }
            population.clear();
            for (T individual : correctlyTypedPopulation) {
                population.add(individual);
            }
            population.sort();

            // 2.6. Update best individual
            T currentBest = population.getBest();
            if (isFirstBetter(currentBest, best)) {
                best = (T) currentBest.copy();
            }

            generation++;
            if (listener != null) {
                listener.onGenerationDone(generation, population.getBest(), population);
            }
            if (context != null && context.getEvents() != null) {
                context.getEvents().publish(new GenerationCompleted("fda", generation, population.getBest()));
            }
        }
        
        if (context != null && context.getEvents() != null) {
            context.getEvents().publish(new AlgorithmTerminated("fda", generation));
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

