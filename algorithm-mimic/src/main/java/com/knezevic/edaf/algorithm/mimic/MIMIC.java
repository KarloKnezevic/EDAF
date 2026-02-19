package com.knezevic.edaf.algorithm.mimic;

import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.core.impl.AbstractAlgorithm;
import com.knezevic.edaf.genotype.binary.BinaryIndividual;

/**
 * The MIMIC algorithm.
 */
public class MIMIC extends AbstractAlgorithm<BinaryIndividual> {

    private final Population<BinaryIndividual> population;
    private final Selection<BinaryIndividual> selection;
    private final Statistics<BinaryIndividual> statistics;
    private final TerminationCondition<BinaryIndividual> terminationCondition;
    private final int selectionSize;

    public MIMIC(Problem<BinaryIndividual> problem, Population<BinaryIndividual> population,
                 Selection<BinaryIndividual> selection, Statistics<BinaryIndividual> statistics,
                 TerminationCondition<BinaryIndividual> terminationCondition, int selectionSize) {
        super(problem, "mimic");
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
            Population<BinaryIndividual> selected = selection.select(population, selectionSize);

            // 2.2. Build probabilistic model
            statistics.estimate(selected);

            // 2.3. Sample new individuals
            Population<BinaryIndividual> newPopulation = statistics.sample(population.getSize());

            // 2.4. Evaluate new individuals
            evaluateAndPublish(newPopulation, getGeneration());

            // 2.5. Elitism: preserve the best individual from current population
            BinaryIndividual bestFromCurrent = population.getBest();

            // 2.6. Replace old population
            population.clear();
            for (int i = 0; i < newPopulation.getSize(); i++) {
                population.add(newPopulation.getIndividual(i));
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
    public Population<BinaryIndividual> getPopulation() {
        return population;
    }
}
