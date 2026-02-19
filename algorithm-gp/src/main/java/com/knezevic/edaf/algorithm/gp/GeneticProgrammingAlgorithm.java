package com.knezevic.edaf.algorithm.gp;

import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.core.impl.AbstractAlgorithm;
import com.knezevic.edaf.genotype.tree.TreeIndividual;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A standard Genetic Programming algorithm implementation.
 * It evolves a population of program trees to solve a given problem.
 */
public class GeneticProgrammingAlgorithm extends AbstractAlgorithm<TreeIndividual> {

    private final Population<TreeIndividual> population;
    private final Selection<TreeIndividual> selection;
    private final Crossover<TreeIndividual> crossover;
    private final Mutation<TreeIndividual> mutation;
    private final TerminationCondition<TreeIndividual> terminationCondition;
    private final double crossoverRate;
    private final double mutationRate;
    private final int elitismSize;
    private final Random random;

    public GeneticProgrammingAlgorithm(Problem<TreeIndividual> problem, Population<TreeIndividual> population,
                                       Selection<TreeIndividual> selection, Crossover<TreeIndividual> crossover,
                                       Mutation<TreeIndividual> mutation, TerminationCondition<TreeIndividual> terminationCondition,
                                       double crossoverRate, double mutationRate, int elitismSize, Random random) {
        super(problem, "gp");
        this.population = population;
        this.selection = selection;
        this.crossover = crossover;
        this.mutation = mutation;
        this.terminationCondition = terminationCondition;
        this.crossoverRate = crossoverRate;
        this.mutationRate = mutationRate;
        this.elitismSize = elitismSize;
        this.random = random != null ? random : new Random();
    }

    @Override
    public void run() {
        publishAlgorithmStarted();

        // Initial evaluation (sequential for GP)
        long t0 = System.nanoTime();
        for (int i = 0; i < population.getSize(); i++) {
            problem.evaluate(population.getIndividual(i));
        }
        long t1 = System.nanoTime();
        publishEvaluationCompleted(0, population.getSize(), t1 - t0);
        population.sort();
        setBest(population.getBest());

        while (!terminationCondition.shouldTerminate(this)) {
            incrementGeneration();
            List<TreeIndividual> newPopulation = new ArrayList<>();

            // Elitism
            for (int i = 0; i < elitismSize; i++) {
                newPopulation.add((TreeIndividual) population.getIndividual(i).copy());
            }

            // Create new individuals
            while (newPopulation.size() < population.getSize()) {
                // Selection
                Population<TreeIndividual> parents = selection.select(population, 2);
                TreeIndividual parent1 = parents.getIndividual(0);
                TreeIndividual parent2 = parents.getIndividual(1);

                // Crossover
                TreeIndividual offspring;
                if (random.nextDouble() < crossoverRate) {
                    offspring = crossover.crossover(parent1, parent2);
                } else {
                    offspring = (TreeIndividual) parent1.copy();
                }

                // Mutation
                if (random.nextDouble() < mutationRate) {
                    mutation.mutate(offspring);
                }

                newPopulation.add(offspring);
            }

            // Evaluate only new individuals (skip elites that are already evaluated) - sequential
            long e0 = System.nanoTime();
            for (int i = elitismSize; i < newPopulation.size(); i++) {
                problem.evaluate(newPopulation.get(i));
            }
            long e1 = System.nanoTime();
            publishEvaluationCompleted(getGeneration(), newPopulation.size() - elitismSize, e1 - e0);

            for (int i = 0; i < newPopulation.size(); i++) {
                population.setIndividual(i, newPopulation.get(i));
            }

            population.sort();
            TreeIndividual currentBest = population.getBest();
            updateBestIfBetter(currentBest);

            notifyListener();
            publishGenerationCompleted();
        }
        publishAlgorithmTerminated();
    }

    @Override
    public Population<TreeIndividual> getPopulation() {
        return population;
    }
}
