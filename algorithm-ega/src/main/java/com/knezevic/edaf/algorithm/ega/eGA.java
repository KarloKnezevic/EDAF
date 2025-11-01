package com.knezevic.edaf.algorithm.ega;

import com.knezevic.edaf.core.api.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.knezevic.edaf.core.runtime.ExecutionContext;
import com.knezevic.edaf.core.runtime.SupportsExecutionContext;
import com.knezevic.edaf.core.runtime.GenerationCompleted;
import com.knezevic.edaf.core.runtime.PopulationStatistics;
import com.knezevic.edaf.core.runtime.AlgorithmStarted;
import com.knezevic.edaf.core.runtime.AlgorithmTerminated;

/**
 * An eliminative Genetic Algorithm (eGA).
 *
 * @param <T> The type of individual in the population.
 */
public class eGA<T extends Individual> implements Algorithm<T>, SupportsExecutionContext {

    private final Problem<T> problem;
    private final Population<T> population;
    private final Selection<T> selection;
    private final Crossover<T> crossover;
    private final Mutation<T> mutation;
    private final TerminationCondition<T> terminationCondition;

    private T best;
    private int generation;
    private ProgressListener listener;
    private ExecutionContext context;

    public eGA(Problem<T> problem, Population<T> population, Selection<T> selection,
               Crossover<T> crossover, Mutation<T> mutation,
               TerminationCondition<T> terminationCondition) {
        this.problem = problem;
        this.population = population;
        this.selection = selection;
        this.crossover = crossover;
        this.mutation = mutation;
        this.terminationCondition = terminationCondition;
    }

    @Override
    public void run() {
        if (context != null && context.getEvents() != null) {
            context.getEvents().publish(new AlgorithmStarted("ega"));
        }
        // 1. Initialize population
        long t0 = System.nanoTime();
        evaluatePopulation(population);
        long t1 = System.nanoTime();
        if (context != null && context.getEvents() != null) {
            context.getEvents().publish(new com.knezevic.edaf.core.runtime.EvaluationCompleted("ega", 0, population.getSize(), t1 - t0));
        }
        population.sort();
        best = (T) population.getBest().copy();
        generation = 0;

        // 2. Run generations
        while (!terminationCondition.shouldTerminate(this)) {
            // 2.1. Select parents
            Population<T> parents = selection.select(population, 2);

            // 2.2. Crossover and mutation
            T offspring = crossover.crossover(parents.getIndividual(0), parents.getIndividual(1));
            mutation.mutate(offspring);
            problem.evaluate(offspring);

            // 2.3. Update population (elitism: never replace the best individual)
            population.sort();
            T currentBest = population.getBest();
            T worst = population.getWorst();
            
            // Only replace worst if offspring is better AND worst is not the current best
            if (isFirstBetter(offspring, worst) && worst != currentBest) {
                population.remove(worst);
                population.add(offspring);
            } else if (isFirstBetter(offspring, currentBest)) {
                // Offspring is better than current best, replace worst with offspring
                population.remove(worst);
                population.add(offspring);
            }
            
            population.sort();

            // 2.4. Update best individual
            currentBest = population.getBest();
            if (isFirstBetter(currentBest, best)) {
                best = (T) currentBest.copy();
            }

            generation++;
            if (listener != null) {
                listener.onGenerationDone(generation, population.getBest(), population);
            }
            if (context != null && context.getEvents() != null) {
                PopulationStatistics.Statistics stats = PopulationStatistics.calculate(population);
                context.getEvents().publish(new GenerationCompleted("ega", generation, population.getBest(),
                    stats.best(), stats.worst(), stats.avg(), stats.std()));
            }
        }
        if (context != null && context.getEvents() != null) {
            context.getEvents().publish(new AlgorithmTerminated("ega", generation));
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
