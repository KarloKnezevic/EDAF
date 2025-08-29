package hr.fer.zemris.edaf.algorithm.ega;

import hr.fer.zemris.edaf.core.api.*;
import hr.fer.zemris.edaf.core.impl.*;
import hr.fer.zemris.edaf.core.api.*;
import hr.fer.zemris.edaf.core.impl.*;
import hr.fer.zemris.edaf.core.api.*;
import hr.fer.zemris.edaf.core.impl.*;
import hr.fer.zemris.edaf.core.api.*;
import hr.fer.zemris.edaf.core.impl.*;
import hr.fer.zemris.edaf.core.api.*;
import hr.fer.zemris.edaf.core.impl.*;
import hr.fer.zemris.edaf.core.api.*;
import hr.fer.zemris.edaf.core.impl.*;
import hr.fer.zemris.edaf.core.api.*;
import hr.fer.zemris.edaf.core.impl.*;
import hr.fer.zemris.edaf.core.api.*;
import hr.fer.zemris.edaf.core.impl.*;
import hr.fer.zemris.edaf.core.api.*;
import hr.fer.zemris.edaf.core.impl.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * An eliminative Genetic Algorithm (eGA).
 *
 * @param <T> The type of individual in the population.
 */
public class eGA<T extends Individual> implements Algorithm<T> {

    private final Problem<T> problem;
    private final Population<T> population;
    private final Selection<T> selection;
    private final Crossover<T> crossover;
    private final Mutation<T> mutation;
    private final TerminationCondition<T> terminationCondition;

    private T best;
    private int generation;
    private ProgressListener listener;

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
        // 1. Initialize population
        evaluatePopulation(population);
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

            // 2.3. Replace worst individual
            population.sort();
            T worst = population.getWorst();
            if (offspring.getFitness() < worst.getFitness()) {
                population.remove(worst);
                population.add(offspring);
            }

            // 2.4. Update best individual
            population.sort();
            T currentBest = population.getBest();
            if (currentBest.getFitness() < best.getFitness()) {
                best = (T) currentBest.copy();
            }

            generation++;
            if (listener != null) {
                listener.onGenerationDone(generation, population.getBest(), population);
            }
        }
    }

    private void evaluatePopulation(Population<T> population) {
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
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
        executor.shutdown();
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
