package com.knezevic.edaf.examples;

import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.core.impl.SimplePopulation;
import com.knezevic.edaf.genotype.permutation.PermutationGenotype;
import com.knezevic.edaf.genotype.permutation.PermutationIndividual;
import com.knezevic.edaf.genotype.permutation.crossing.OrderCrossover;
import com.knezevic.edaf.genotype.permutation.mutation.InversionMutation;
import com.knezevic.edaf.selection.TournamentSelection;
import com.knezevic.edaf.algorithm.ega.eGA;

import java.util.Arrays;
import java.util.Map;
import java.util.Random;

/**
 * Solves a random TSP instance using an eliminative genetic algorithm (eGA)
 * with order crossover and inversion mutation.
 * <p>
 * Demonstrates the permutation genotype, TSP problem setup, and
 * real-world combinatorial optimization with the EDAF framework.
 * The eGA performs steady-state replacement: each generation produces
 * one offspring that replaces the worst individual, with built-in elitism
 * that never replaces the best.
 */
public class TSPExample {

    private static final int NUM_CITIES = 20;
    private static final int POPULATION_SIZE = 200;
    private static final int MAX_GENERATIONS = 5000;
    private static final long SEED = 42;

    public static void main(String[] args) {
        System.out.println("=== EDAF TSP Example: " + NUM_CITIES + "-city random instance ===");
        Random random = new Random(SEED);

        // Generate random city coordinates and compute distance matrix
        double[][] coords = new double[NUM_CITIES][2];
        for (int i = 0; i < NUM_CITIES; i++) {
            coords[i][0] = random.nextDouble() * 100;
            coords[i][1] = random.nextDouble() * 100;
        }

        // Print city coordinates
        System.out.println("City coordinates:");
        for (int i = 0; i < NUM_CITIES; i++) {
            System.out.println(String.format("  City %2d: (%.2f, %.2f)", i, coords[i][0], coords[i][1]));
        }
        System.out.println();

        // Compute symmetric distance matrix
        double[][] distances = new double[NUM_CITIES][NUM_CITIES];
        for (int i = 0; i < NUM_CITIES; i++) {
            for (int j = i + 1; j < NUM_CITIES; j++) {
                double dx = coords[i][0] - coords[j][0];
                double dy = coords[i][1] - coords[j][1];
                distances[i][j] = Math.hypot(dx, dy);
                distances[j][i] = distances[i][j];
            }
        }

        // Create TSP problem using inline Problem implementation (minimization)
        Problem<PermutationIndividual> problem = new Problem<>() {
            @Override
            public void evaluate(PermutationIndividual individual) {
                int[] tour = individual.getGenotype();
                double totalDistance = 0;
                for (int i = 0; i < tour.length; i++) {
                    int from = tour[i];
                    int to = tour[(i + 1) % tour.length];
                    totalDistance += distances[from][to];
                }
                individual.setFitness(totalDistance);
            }

            @Override
            public OptimizationType getOptimizationType() {
                return OptimizationType.min;
            }
        };

        // Create initial population of random permutations
        PermutationGenotype genotype = new PermutationGenotype(NUM_CITIES, random);
        Population<PermutationIndividual> population = new SimplePopulation<>(OptimizationType.min);
        for (int i = 0; i < POPULATION_SIZE; i++) {
            PermutationIndividual ind = new PermutationIndividual(genotype.create());
            problem.evaluate(ind);
            population.add(ind);
        }
        population.sort();
        System.out.println("Initial best tour length: " + String.format("%.2f", population.getBest().getFitness()));

        // Create genetic operators
        Selection<PermutationIndividual> selection = new TournamentSelection<>(random, 5);
        Crossover<PermutationIndividual> crossover = new OrderCrossover(random);
        Mutation<PermutationIndividual> mutation = new InversionMutation(random);
        TerminationCondition<PermutationIndividual> termination = alg -> alg.getGeneration() >= MAX_GENERATIONS;

        // Run eGA (eliminative GA with built-in elitism)
        eGA<PermutationIndividual> algorithm = new eGA<>(
                problem, population, selection, crossover, mutation, termination);

        System.out.println("Running eGA for up to " + MAX_GENERATIONS + " generations...");
        algorithm.run();

        // Print results
        PermutationIndividual best = algorithm.getBest();
        System.out.println();
        System.out.println("=== Results ===");
        System.out.println("Generations:     " + algorithm.getGeneration());
        System.out.println("Best tour length: " + String.format("%.2f", best.getFitness()));
        System.out.println("Best tour:        " + Arrays.toString(best.getGenotype()));
    }
}
