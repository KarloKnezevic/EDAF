package com.knezevic.edaf.examples;

import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.core.impl.SimplePopulation;
import com.knezevic.edaf.genotype.binary.BinaryGenotype;
import com.knezevic.edaf.genotype.binary.BinaryIndividual;
import com.knezevic.edaf.selection.TournamentSelection;
import com.knezevic.edaf.statistics.distribution.BernoulliDistribution;

import java.util.*;

/**
 * Feature selection using UMDA on a synthetic classification dataset.
 * Each bit in the genotype represents whether a feature is included.
 * Fitness = classification accuracy - sparsity penalty.
 * Demonstrates binary genotype on a practical ML task.
 */
public class FeatureSelectionExample {

    private static final int NUM_FEATURES = 20;
    private static final int NUM_RELEVANT = 5;   // Only 5 features are actually relevant
    private static final int NUM_SAMPLES = 200;
    private static final int POPULATION_SIZE = 100;
    private static final int MAX_GENERATIONS = 200;
    private static final double SPARSITY_PENALTY = 0.005;
    private static final long SEED = 42;

    public static void main(String[] args) {
        System.out.println("=== EDAF Feature Selection Example ===");
        System.out.println("Features: " + NUM_FEATURES + " (only " + NUM_RELEVANT + " are relevant)");
        System.out.println("Samples: " + NUM_SAMPLES + ", Population: " + POPULATION_SIZE);
        System.out.println();

        Random random = new Random(SEED);

        // Generate synthetic dataset with known relevant features
        double[][] data = new double[NUM_SAMPLES][NUM_FEATURES];
        int[] labels = new int[NUM_SAMPLES];
        generateDataset(data, labels, random);

        // Baseline: accuracy using all features
        double baselineAccuracy = evaluateAccuracy(data, labels, allFeatures(NUM_FEATURES));
        System.out.println("Baseline accuracy (all " + NUM_FEATURES + " features): " +
                String.format("%.4f", baselineAccuracy));

        // Create feature selection problem
        Problem<BinaryIndividual> problem = new Problem<>() {
            @Override
            public void evaluate(BinaryIndividual individual) {
                byte[] genotype = individual.getGenotype();
                boolean[] selected = new boolean[genotype.length];
                int count = 0;
                for (int i = 0; i < genotype.length; i++) {
                    selected[i] = genotype[i] == 1;
                    if (selected[i]) count++;
                }
                if (count == 0) {
                    individual.setFitness(0);
                    return;
                }
                double accuracy = evaluateAccuracy(data, labels, selected);
                individual.setFitness(accuracy - SPARSITY_PENALTY * count);
            }

            @Override
            public OptimizationType getOptimizationType() {
                return OptimizationType.max;
            }
        };

        // Create genotype factory and population
        BinaryGenotype genotype = new BinaryGenotype(NUM_FEATURES, random);
        Population<BinaryIndividual> population = new SimplePopulation<>(OptimizationType.max);
        for (int i = 0; i < POPULATION_SIZE; i++) {
            BinaryIndividual ind = new BinaryIndividual(genotype.create());
            problem.evaluate(ind);
            population.add(ind);
        }
        population.sort();

        // Run UMDA with tournament selection for the selection phase
        Selection<BinaryIndividual> selection = new TournamentSelection<>(random, 3);
        Statistics<BinaryIndividual> stats = new BernoulliDistribution(genotype, random);
        TerminationCondition<BinaryIndividual> termination = alg -> alg.getGeneration() >= MAX_GENERATIONS;
        var algorithm = new com.knezevic.edaf.algorithm.umda.Umda<>(problem, population, selection,
                stats, termination, POPULATION_SIZE / 2);
        algorithm.run();

        // Results
        BinaryIndividual best = algorithm.getBest();
        byte[] bestGenotype = best.getGenotype();
        System.out.println("\nAfter " + algorithm.getGeneration() + " generations:");
        System.out.println("Best fitness: " + String.format("%.4f", best.getFitness()));

        List<Integer> selectedFeatures = new ArrayList<>();
        for (int i = 0; i < bestGenotype.length; i++) {
            if (bestGenotype[i] == 1) selectedFeatures.add(i);
        }
        System.out.println("Selected features: " + selectedFeatures);
        System.out.println("Number of features selected: " + selectedFeatures.size() + " / " + NUM_FEATURES);

        boolean[] selectedMask = new boolean[NUM_FEATURES];
        for (int idx : selectedFeatures) selectedMask[idx] = true;
        double selectedAccuracy = evaluateAccuracy(data, labels, selectedMask);
        System.out.println("Accuracy with selected features: " + String.format("%.4f", selectedAccuracy));
        System.out.println("Accuracy improvement over baseline: " +
                String.format("%.4f", selectedAccuracy - baselineAccuracy));

        // Check how many of the truly relevant features were found
        int relevantFound = 0;
        for (int i = 0; i < NUM_RELEVANT; i++) {
            if (selectedMask[i]) relevantFound++;
        }
        System.out.println("Relevant features recovered: " + relevantFound + " / " + NUM_RELEVANT);
    }

    private static void generateDataset(double[][] data, int[] labels, Random random) {
        // Features 0..NUM_RELEVANT-1 are informative, rest are noise
        for (int i = 0; i < NUM_SAMPLES; i++) {
            labels[i] = random.nextInt(2);
            for (int j = 0; j < NUM_FEATURES; j++) {
                if (j < NUM_RELEVANT) {
                    // Relevant features: class-dependent with some noise
                    data[i][j] = labels[i] * 2.0 + random.nextGaussian() * 0.5;
                } else {
                    // Noise features: random values unrelated to class
                    data[i][j] = random.nextGaussian();
                }
            }
        }
    }

    private static boolean[] allFeatures(int n) {
        boolean[] all = new boolean[n];
        Arrays.fill(all, true);
        return all;
    }

    /**
     * Simple 1-nearest-neighbor leave-one-out cross-validation accuracy.
     */
    private static double evaluateAccuracy(double[][] data, int[] labels, boolean[] selectedFeatures) {
        int correct = 0;
        int n = data.length;
        for (int i = 0; i < n; i++) {
            double minDist = Double.MAX_VALUE;
            int nearestLabel = -1;
            for (int j = 0; j < n; j++) {
                if (i == j) continue;
                double dist = distance(data[i], data[j], selectedFeatures);
                if (dist < minDist) {
                    minDist = dist;
                    nearestLabel = labels[j];
                }
            }
            if (nearestLabel == labels[i]) correct++;
        }
        return (double) correct / n;
    }

    private static double distance(double[] a, double[] b, boolean[] selected) {
        double sum = 0;
        for (int i = 0; i < a.length; i++) {
            if (selected[i]) {
                double d = a[i] - b[i];
                sum += d * d;
            }
        }
        return Math.sqrt(sum);
    }
}
