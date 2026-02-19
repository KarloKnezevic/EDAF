package com.knezevic.edaf.examples;

import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.core.impl.SimplePopulation;
import com.knezevic.edaf.genotype.binary.BinaryGenotype;
import com.knezevic.edaf.genotype.binary.BinaryIndividual;
import com.knezevic.edaf.statistics.distribution.BernoulliDistribution;
import com.knezevic.edaf.statistics.mimic.MimicStatistics;
import com.knezevic.edaf.algorithm.cem.CemStatistics;
import com.knezevic.edaf.testing.problems.MaxOnes;
import com.knezevic.edaf.selection.TournamentSelection;

import java.util.*;

/**
 * Compares 4 EDA algorithms (UMDA, PBIL, MIMIC, CEM) on the MaxOnes problem
 * over multiple independent runs with different random seeds.
 * <p>
 * Demonstrates programmatic algorithm creation without configuration files.
 * For each algorithm, the program tracks best fitness achieved and the number
 * of generations needed, then prints a formatted comparison table.
 */
public class AlgorithmComparisonExample {

    private static final int GENOTYPE_LENGTH = 50;
    private static final int POPULATION_SIZE = 200;
    private static final int MAX_GENERATIONS = 500;
    private static final int NUM_RUNS = 10;
    private static final int SELECTION_SIZE = POPULATION_SIZE / 2;

    public static void main(String[] args) {
        System.out.println("=== EDAF Algorithm Comparison: 4 EDAs on MaxOnes(" + GENOTYPE_LENGTH + ") ===");
        System.out.println("Population: " + POPULATION_SIZE + ", Max Generations: " + MAX_GENERATIONS + ", Runs: " + NUM_RUNS);
        System.out.println();

        Map<String, List<RunResult>> results = new LinkedHashMap<>();
        results.put("UMDA", runAlgorithm("umda"));
        results.put("PBIL", runAlgorithm("pbil"));
        results.put("MIMIC", runAlgorithm("mimic"));
        results.put("CEM", runAlgorithm("cem"));

        printComparisonTable(results);
    }

    private static List<RunResult> runAlgorithm(String algorithmName) {
        List<RunResult> runs = new ArrayList<>();
        for (int seed = 0; seed < NUM_RUNS; seed++) {
            Random random = new Random(seed);

            // Create the MaxOnes problem (maximization)
            Map<String, Object> problemParams = Map.of("optimizationType", OptimizationType.max);
            Problem<BinaryIndividual> problem = new MaxOnes(problemParams);

            // Create genotype factory for binary strings
            BinaryGenotype genotype = new BinaryGenotype(GENOTYPE_LENGTH, random);

            // Build initial population
            Population<BinaryIndividual> population = new SimplePopulation<>(OptimizationType.max);
            for (int i = 0; i < POPULATION_SIZE; i++) {
                BinaryIndividual ind = new BinaryIndividual(genotype.create());
                problem.evaluate(ind);
                population.add(ind);
            }
            population.sort();

            // Termination: stop at max generations or when a perfect solution is found
            TerminationCondition<BinaryIndividual> termination = alg ->
                    alg.getGeneration() >= MAX_GENERATIONS
                            || (alg.getBest() != null && alg.getBest().getFitness() >= GENOTYPE_LENGTH);

            Algorithm<BinaryIndividual> algorithm = createAlgorithm(
                    algorithmName, problem, population, genotype, termination, random);
            if (algorithm == null) {
                continue;
            }

            algorithm.run();

            double bestFitness = algorithm.getBest() != null ? algorithm.getBest().getFitness() : 0;
            int generations = algorithm.getGeneration();
            runs.add(new RunResult(bestFitness, generations));
        }
        System.out.println("  " + algorithmName.toUpperCase() + ": " + runs.size() + " runs completed");
        return runs;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Algorithm<BinaryIndividual> createAlgorithm(
            String name, Problem<BinaryIndividual> problem, Population<BinaryIndividual> population,
            BinaryGenotype genotype, TerminationCondition<BinaryIndividual> termination, Random random) {

        Selection<BinaryIndividual> selection = new TournamentSelection<>(random, 3);

        return switch (name) {
            case "umda" -> {
                Statistics<BinaryIndividual> stats = new BernoulliDistribution(genotype, random);
                yield new com.knezevic.edaf.algorithm.umda.Umda<>(
                        problem, population, selection, stats, termination, SELECTION_SIZE);
            }
            case "pbil" -> {
                Statistics<BinaryIndividual> stats = new BernoulliDistribution(genotype, random);
                yield new com.knezevic.edaf.algorithm.pbil.Pbil<>(
                        problem, stats, termination, POPULATION_SIZE, 0.1);
            }
            case "mimic" -> {
                Statistics<BinaryIndividual> mimicStats = new MimicStatistics(genotype, random);
                yield new com.knezevic.edaf.algorithm.mimic.MIMIC(
                        problem, population, selection, mimicStats, termination, SELECTION_SIZE);
            }
            case "cem" -> {
                CemStatistics cemStats = new CemStatistics(GENOTYPE_LENGTH, random, true);
                yield (Algorithm) new com.knezevic.edaf.algorithm.cem.CEM<>(
                        (Problem) problem, (Statistics) cemStats, (TerminationCondition) termination,
                        POPULATION_SIZE, 0.2, 0.7);
            }
            default -> null;
        };
    }

    private static void printComparisonTable(Map<String, List<RunResult>> results) {
        System.out.println();
        System.out.println(String.format("%-10s | %12s | %8s | %16s",
                "Algorithm", "Avg Best", "Std Dev", "Avg Generations"));
        System.out.println("-".repeat(56));
        for (var entry : results.entrySet()) {
            List<RunResult> runs = entry.getValue();
            double avgBest = runs.stream().mapToDouble(r -> r.bestFitness).average().orElse(0);
            double stdBest = std(runs.stream().mapToDouble(r -> r.bestFitness).toArray());
            double avgGen = runs.stream().mapToDouble(r -> r.generations).average().orElse(0);
            System.out.println(String.format("%-10s | %12.2f | %8.2f | %16.1f",
                    entry.getKey(), avgBest, stdBest, avgGen));
        }
        System.out.println();
        System.out.println("Target fitness: " + GENOTYPE_LENGTH + " (all ones)");
    }

    private static double std(double[] values) {
        double mean = Arrays.stream(values).average().orElse(0);
        double variance = Arrays.stream(values).map(v -> (v - mean) * (v - mean)).average().orElse(0);
        return Math.sqrt(variance);
    }

    record RunResult(double bestFitness, int generations) {}
}
