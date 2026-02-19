package com.knezevic.edaf.algorithm.bmda;

import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.core.impl.AbstractAlgorithm;
import com.knezevic.edaf.core.impl.SimplePopulation;

/**
 * The Bivariate Marginal Distribution Algorithm (BMDA).
 *
 * @param <T> The type of individual in the population.
 */
public class Bmda<T extends Individual> extends AbstractAlgorithm<T> {

    private final Population<T> population;
    private final Selection<T> selection;
    private final Statistics<T> statistics;
    private final TerminationCondition<T> terminationCondition;
    private final int selectionSize;

    public Bmda(Problem<T> problem, Population<T> population, Selection<T> selection,
                Statistics<T> statistics, TerminationCondition<T> terminationCondition,
                int selectionSize) {
        super(problem, "bmda");
        this.population = population;
        this.selection = selection;
        this.statistics = statistics;
        this.terminationCondition = terminationCondition;
        this.selectionSize = selectionSize;
    }

    @Override
    public void run() {
        // 1. Initialize population
        publishAlgorithmStarted();
        evaluateAndPublish(population, 0);
        population.sort();
        setBest(population.getBest());
        setGeneration(0);

        // 2. Run generations
        while (!terminationCondition.shouldTerminate(this)) {
            // 2.1. Select best individuals
            Population<T> selected = selection.select(population, selectionSize);

            // 2.2. Build probabilistic model
            statistics.estimate(selected);

            // 2.3. Sample new individuals
            Population<T> newPopulation = statistics.sample(population.getSize());

            // 2.4. Evaluate new individuals
            evaluateAndPublish(newPopulation, getGeneration());

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
            applyElitism(population, bestFromCurrent);

            // 2.8. Update best individual
            updateBestIfBetter(population.getBest());

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
