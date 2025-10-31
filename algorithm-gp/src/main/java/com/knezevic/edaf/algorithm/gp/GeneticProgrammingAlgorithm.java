package com.knezevic.edaf.algorithm.gp;

import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.genotype.tree.TreeIndividual;
import com.knezevic.edaf.genotype.tree.operators.crossover.TreeCrossover;
import com.knezevic.edaf.genotype.tree.operators.mutation.TreeMutation;

import java.util.ArrayList;
import java.util.List;
import com.knezevic.edaf.core.runtime.ExecutionContext;
import com.knezevic.edaf.core.runtime.SupportsExecutionContext;

/**
 * A standard Genetic Programming algorithm implementation.
 * It evolves a population of program trees to solve a given problem.
 */
public class GeneticProgrammingAlgorithm implements Algorithm<TreeIndividual>, SupportsExecutionContext {

    private final Problem<TreeIndividual> problem;
    private final Population<TreeIndividual> population;
    private final Selection<TreeIndividual> selection;
    private final Crossover<TreeIndividual> crossover;
    private final Mutation<TreeIndividual> mutation;
    private final TerminationCondition<TreeIndividual> terminationCondition;
    private final double crossoverRate;
    private final double mutationRate;
    private final int elitismSize;

    private TreeIndividual best;
    private int generation;
    private ProgressListener listener;
    private ExecutionContext context;

    public GeneticProgrammingAlgorithm(Problem<TreeIndividual> problem, Population<TreeIndividual> population,
                                       Selection<TreeIndividual> selection, Crossover<TreeIndividual> crossover,
                                       Mutation<TreeIndividual> mutation, TerminationCondition<TreeIndividual> terminationCondition,
                                       double crossoverRate, double mutationRate, int elitismSize) {
        this.problem = problem;
        this.population = population;
        this.selection = selection;
        this.crossover = crossover;
        this.mutation = mutation;
        this.terminationCondition = terminationCondition;
        this.crossoverRate = crossoverRate;
        this.mutationRate = mutationRate;
        this.elitismSize = elitismSize;
    }

    @Override
    public void run() {
        // Initial evaluation
        long t0 = System.nanoTime();
        for (int i = 0; i < population.getSize(); i++) {
            problem.evaluate(population.getIndividual(i));
        }
        long t1 = System.nanoTime();
        if (context != null && context.getEvents() != null) {
            context.getEvents().publish(new com.knezevic.edaf.core.runtime.EvaluationCompleted("gp", 0, population.getSize(), t1 - t0));
        }
        population.sort();
        best = (TreeIndividual) population.getBest().copy();

        while (!terminationCondition.shouldTerminate(this)) {
            generation++;
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
                if (Math.random() < crossoverRate) {
                    offspring = crossover.crossover(parent1, parent2);
                } else {
                    offspring = (TreeIndividual) parent1.copy();
                }

                // Mutation
                if (Math.random() < mutationRate) {
                    mutation.mutate(offspring);
                }

                newPopulation.add(offspring);
            }

            // Evaluate and replace
            long e0 = System.nanoTime();
            for (TreeIndividual individual : newPopulation) {
                problem.evaluate(individual);
            }
            long e1 = System.nanoTime();
            if (context != null && context.getEvents() != null) {
                context.getEvents().publish(new com.knezevic.edaf.core.runtime.EvaluationCompleted("gp", generation, newPopulation.size(), e1 - e0));
            }

            for(int i = 0; i < newPopulation.size(); i++) {
                population.setIndividual(i, newPopulation.get(i));
            }

            population.sort();
            TreeIndividual currentBest = (TreeIndividual) population.getBest();
            if (isFirstBetter(currentBest, best)) {
                best = (TreeIndividual) currentBest.copy();
            }

            if (listener != null) {
                listener.onGenerationDone(generation, best, population);
            }
        }
    }

    @Override
    public TreeIndividual getBest() {
        return best;
    }

    @Override
    public int getGeneration() {
        return generation;
    }

    @Override
    public Population<TreeIndividual> getPopulation() {
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
