package com.knezevic.edaf.algorithm.ega;

import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.core.impl.AbstractAlgorithm;

/**
 * An eliminative Genetic Algorithm (eGA).
 *
 * @param <T> The type of individual in the population.
 */
public class eGA<T extends Individual> extends AbstractAlgorithm<T> {

    private final Population<T> population;
    private final Selection<T> selection;
    private final Crossover<T> crossover;
    private final Mutation<T> mutation;
    private final TerminationCondition<T> terminationCondition;

    public eGA(Problem<T> problem, Population<T> population, Selection<T> selection,
               Crossover<T> crossover, Mutation<T> mutation,
               TerminationCondition<T> terminationCondition) {
        super(problem, "ega");
        this.population = population;
        this.selection = selection;
        this.crossover = crossover;
        this.mutation = mutation;
        this.terminationCondition = terminationCondition;
    }

    @Override
    public void run() {
        publishAlgorithmStarted();

        // 1. Initialize population
        evaluateAndPublish(population, 0);
        population.sort();
        setBest(population.getBest());
        setGeneration(0);

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
            updateBestIfBetter(currentBest);

            incrementGeneration();
            notifyListener();
            publishGenerationCompleted();
        }
        publishAlgorithmTerminated();
    }

    @Override
    public Population<T> getPopulation() {
        return population;
    }
}
