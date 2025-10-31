package com.knezevic.edaf.algorithm.gga;

import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.core.impl.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.knezevic.edaf.core.runtime.ExecutionContext;
import com.knezevic.edaf.core.runtime.SupportsExecutionContext;
import com.knezevic.edaf.core.runtime.GenerationCompleted;
import com.knezevic.edaf.core.runtime.AlgorithmStarted;
import com.knezevic.edaf.core.runtime.AlgorithmTerminated;

/**
 * A generational Genetic Algorithm (gGA).
 * <p>
 * gGA is a traditional genetic algorithm where the entire population is replaced in each generation.
 * It follows the typical evolutionary cycle of selection, crossover, and mutation.
 * </p>
 * <p>
 * The algorithm works as follows:
 * <ol>
 *     <li>Initialize a population of individuals.</li>
 *     <li>Evaluate the fitness of each individual in the population.</li>
 *     <li>While the termination condition is not met:
 *         <ol>
 *             <li>Create a new empty population.</li>
 *             <li>Apply elitism: Copy a number of the best individuals from the current population to the new population.</li>
 *             <li>Fill the rest of the new population by repeatedly selecting two parents, performing crossover to create an offspring, and mutating the offspring.</li>
 *             <li>Replace the current population with the new population.</li>
 *             <li>Evaluate the fitness of the individuals in the new population.</li>
 *         </ol>
 *     </li>
 * </ol>
 * </p>
 * <p>
 * This implementation is generic and can work with different types of individuals and genetic operators.
 * </p>
 *
 * @param <T> The type of individual in the population.
 */
public class gGA<T extends Individual> implements Algorithm<T>, SupportsExecutionContext {

    private final Problem<T> problem;
    private final Population<T> population;
    private final Selection<T> selection;
    private final Crossover<T> crossover;
    private final Mutation<T> mutation;
    private final TerminationCondition<T> terminationCondition;
    private final int elitism;

    private T best;
    private int generation;
    private ProgressListener listener;
    private ExecutionContext context;

    public gGA(Problem<T> problem, Population<T> population, Selection<T> selection,
               Crossover<T> crossover, Mutation<T> mutation,
               TerminationCondition<T> terminationCondition, int elitism) {
        this.problem = problem;
        this.population = population;
        this.selection = selection;
        this.crossover = crossover;
        this.mutation = mutation;
        this.terminationCondition = terminationCondition;
        this.elitism = elitism;
    }

    @Override
    public void run() {
        if (context != null && context.getEvents() != null) {
            context.getEvents().publish(new AlgorithmStarted("gga"));
        }
        // 1. Initialize population
        long t0 = System.nanoTime();
        evaluatePopulation(population);
        long t1 = System.nanoTime();
        if (context != null && context.getEvents() != null) {
            context.getEvents().publish(new com.knezevic.edaf.core.runtime.EvaluationCompleted("gga", 0, population.getSize(), t1 - t0));
        }
        population.sort();
        best = (T) population.getBest().copy();
        generation = 0;

        // 2. Run generations
        while (!terminationCondition.shouldTerminate(this)) {
            // 2.1. Create new population
            Population<T> newPopulation = new SimplePopulation<>(population.getOptimizationType());

            // 2.2. Elitism
            for (int i = 0; i < elitism; i++) {
                newPopulation.add((T) population.getIndividual(i).copy());
            }

            // 2.3. Crossover and mutation
            while (newPopulation.getSize() < population.getSize()) {
                // Select parents
                Population<T> parents = selection.select(population, 2);
                // Crossover
                T offspring = crossover.crossover(parents.getIndividual(0), parents.getIndividual(1));
                // Mutation
                mutation.mutate(offspring);
                // Add to new population
                newPopulation.add(offspring);
            }

            // 2.4. Evaluate new individuals
            long e0 = System.nanoTime();
            evaluatePopulation(newPopulation);
            long e1 = System.nanoTime();
            if (context != null && context.getEvents() != null) {
                context.getEvents().publish(new com.knezevic.edaf.core.runtime.EvaluationCompleted("gga", generation, newPopulation.getSize(), e1 - e0));
            }

            // 2.5. Replace old population
            population.clear();
            for (T individual : newPopulation) {
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
                context.getEvents().publish(new GenerationCompleted("gga", generation, population.getBest()));
            }
        }
        if (context != null && context.getEvents() != null) {
            context.getEvents().publish(new AlgorithmTerminated("gga", generation));
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
            e.printStackTrace();
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
        if (problem.getOptimizationType() == OptimizationType.min) {
            return first.getFitness() < second.getFitness();
        } else {
            return first.getFitness() > second.getFitness();
        }
    }
}
