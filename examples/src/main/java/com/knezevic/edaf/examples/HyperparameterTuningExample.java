package com.knezevic.edaf.examples;

import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.core.impl.SimplePopulation;
import com.knezevic.edaf.genotype.categorical.CategoricalGenotype;
import com.knezevic.edaf.genotype.categorical.CategoricalIndividual;
import com.knezevic.edaf.genotype.categorical.crossing.CategoricalUniformCrossover;
import com.knezevic.edaf.genotype.categorical.mutation.CategoricalRandomResetMutation;
import com.knezevic.edaf.selection.TournamentSelection;
import com.knezevic.edaf.algorithm.gga.gGA;

import java.util.*;

/**
 * Hyperparameter tuning using categorical genotype with a generational GA.
 * Tunes parameters of a k-NN classifier on a synthetic dataset:
 *   Gene 0: k value (1, 3, 5, 7, 9) - 5 choices
 *   Gene 1: distance metric (Euclidean, Manhattan, Chebyshev) - 3 choices
 *   Gene 2: weighting scheme (uniform, distance-based) - 2 choices
 *   Gene 3: feature scaling (none, standardize, normalize) - 3 choices
 * Demonstrates categorical genotype on a practical hyperparameter optimization task.
 */
public class HyperparameterTuningExample {

    private static final int[] CARDINALITIES = {5, 3, 2, 3};
    private static final int[] K_VALUES = {1, 3, 5, 7, 9};
    private static final String[] DISTANCE_METRICS = {"Euclidean", "Manhattan", "Chebyshev"};
    private static final String[] WEIGHTING = {"uniform", "distance"};
    private static final String[] SCALING = {"none", "standardize", "normalize"};

    private static final int POPULATION_SIZE = 50;
    private static final int MAX_GENERATIONS = 30;
    private static final int NUM_FEATURES = 8;
    private static final int NUM_SAMPLES = 300;
    private static final long SEED = 42;

    public static void main(String[] args) {
        System.out.println("=== EDAF Hyperparameter Tuning Example ===");
        System.out.println("Tuning k-NN classifier on synthetic dataset");
        System.out.println("Search space: " + totalCombinations() + " configurations");
        System.out.println();

        Random random = new Random(SEED);

        // Generate synthetic multi-class dataset
        double[][] data = new double[NUM_SAMPLES][NUM_FEATURES];
        int[] labels = new int[NUM_SAMPLES];
        generateMultiClassDataset(data, labels, random);

        // Split into train (80%) and test (20%)
        int trainSize = (int) (NUM_SAMPLES * 0.8);
        double[][] trainData = Arrays.copyOf(data, trainSize);
        int[] trainLabels = Arrays.copyOf(labels, trainSize);
        double[][] testData = Arrays.copyOfRange(data, trainSize, NUM_SAMPLES);
        int[] testLabels = Arrays.copyOfRange(labels, trainSize, NUM_SAMPLES);

        // Create problem
        Problem<CategoricalIndividual> problem = new Problem<>() {
            @Override
            public void evaluate(CategoricalIndividual individual) {
                int[] config = individual.getGenotype();
                double accuracy = evaluateConfig(config, trainData, trainLabels);
                individual.setFitness(accuracy);
            }

            @Override
            public OptimizationType getOptimizationType() {
                return OptimizationType.max;
            }
        };

        // Create initial population
        CategoricalGenotype genotype = new CategoricalGenotype(CARDINALITIES, random);
        Population<CategoricalIndividual> population = new SimplePopulation<>(OptimizationType.max);
        for (int i = 0; i < POPULATION_SIZE; i++) {
            CategoricalIndividual ind = new CategoricalIndividual(genotype.create());
            problem.evaluate(ind);
            population.add(ind);
        }
        population.sort();
        System.out.println("Initial best config accuracy: " + String.format("%.4f", population.getBest().getFitness()));

        // Create operators and run gGA
        Selection<CategoricalIndividual> selection = new TournamentSelection<>(random, 3);
        Crossover<CategoricalIndividual> crossover = new CategoricalUniformCrossover(random);
        Mutation<CategoricalIndividual> mutation = new CategoricalRandomResetMutation(random, CARDINALITIES, 0.2);
        TerminationCondition<CategoricalIndividual> termination = alg -> alg.getGeneration() >= MAX_GENERATIONS;

        gGA<CategoricalIndividual> algorithm = new gGA<>(problem, population, selection,
                crossover, mutation, termination, 2);
        algorithm.run();

        // Results
        CategoricalIndividual best = algorithm.getBest();
        int[] bestConfig = best.getGenotype();
        System.out.println("\nAfter " + algorithm.getGeneration() + " generations:");
        System.out.println("Best configuration:");
        System.out.println("  k = " + K_VALUES[bestConfig[0]]);
        System.out.println("  Distance = " + DISTANCE_METRICS[bestConfig[1]]);
        System.out.println("  Weighting = " + WEIGHTING[bestConfig[2]]);
        System.out.println("  Scaling = " + SCALING[bestConfig[3]]);
        System.out.println("Training accuracy: " + String.format("%.4f", best.getFitness()));

        // Evaluate on test set
        double testAccuracy = evaluateConfig(bestConfig, trainData, trainLabels, testData, testLabels);
        System.out.println("Test accuracy: " + String.format("%.4f", testAccuracy));
    }

    private static int totalCombinations() {
        int total = 1;
        for (int c : CARDINALITIES) total *= c;
        return total;
    }

    private static void generateMultiClassDataset(double[][] data, int[] labels, Random random) {
        int numClasses = 3;
        double[][] centroids = {
            {2, 2, 0, 0, 0, 0, 0, 0},
            {-2, -2, 0, 0, 0, 0, 0, 0},
            {0, 0, 2, 2, 0, 0, 0, 0}
        };
        for (int i = 0; i < data.length; i++) {
            labels[i] = random.nextInt(numClasses);
            for (int j = 0; j < data[i].length; j++) {
                data[i][j] = centroids[labels[i]][j] + random.nextGaussian() * 1.5;
            }
        }
    }

    private static double evaluateConfig(int[] config, double[][] trainData, int[] trainLabels) {
        return evaluateConfig(config, trainData, trainLabels, trainData, trainLabels);
    }

    private static double evaluateConfig(int[] config, double[][] trainData, int[] trainLabels,
                                         double[][] testData, int[] testLabels) {
        int k = K_VALUES[config[0]];
        int distType = config[1];
        boolean useDistanceWeighting = config[2] == 1;
        int scalingType = config[3];

        // Apply scaling
        double[][] scaledTrain = applyScaling(trainData, scalingType, trainData);
        double[][] scaledTest = applyScaling(testData, scalingType, trainData);

        int correct = 0;
        for (int i = 0; i < scaledTest.length; i++) {
            int predicted = predictKNN(scaledTest[i], scaledTrain, trainLabels, k, distType, useDistanceWeighting);
            if (predicted == testLabels[i]) correct++;
        }
        return (double) correct / testLabels.length;
    }

    private static double[][] applyScaling(double[][] data, int scalingType, double[][] reference) {
        if (scalingType == 0) return data; // none
        int n = data.length;
        int m = data[0].length;
        double[][] scaled = new double[n][m];

        double[] mean = new double[m];
        double[] std = new double[m];
        double[] min = new double[m];
        double[] max = new double[m];
        Arrays.fill(min, Double.MAX_VALUE);
        Arrays.fill(max, -Double.MAX_VALUE);

        for (double[] row : reference) {
            for (int j = 0; j < m; j++) {
                mean[j] += row[j];
                min[j] = Math.min(min[j], row[j]);
                max[j] = Math.max(max[j], row[j]);
            }
        }
        for (int j = 0; j < m; j++) mean[j] /= reference.length;
        for (double[] row : reference) {
            for (int j = 0; j < m; j++) std[j] += (row[j] - mean[j]) * (row[j] - mean[j]);
        }
        for (int j = 0; j < m; j++) std[j] = Math.sqrt(std[j] / reference.length);

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                if (scalingType == 1) { // standardize
                    scaled[i][j] = std[j] > 0 ? (data[i][j] - mean[j]) / std[j] : 0;
                } else { // normalize
                    double range = max[j] - min[j];
                    scaled[i][j] = range > 0 ? (data[i][j] - min[j]) / range : 0;
                }
            }
        }
        return scaled;
    }

    private static int predictKNN(double[] query, double[][] trainData, int[] trainLabels,
                                   int k, int distType, boolean useDistanceWeighting) {
        int n = trainData.length;
        double[] distances = new double[n];
        for (int i = 0; i < n; i++) {
            distances[i] = computeDistance(query, trainData[i], distType);
        }

        // Find k nearest
        Integer[] indices = new Integer[n];
        for (int i = 0; i < n; i++) indices[i] = i;
        Arrays.sort(indices, Comparator.comparingDouble(i -> distances[i]));

        // Vote
        Map<Integer, Double> votes = new HashMap<>();
        for (int i = 0; i < Math.min(k, n); i++) {
            int label = trainLabels[indices[i]];
            double weight = useDistanceWeighting ? 1.0 / (distances[indices[i]] + 1e-10) : 1.0;
            votes.merge(label, weight, Double::sum);
        }

        return votes.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(0);
    }

    private static double computeDistance(double[] a, double[] b, int distType) {
        double sum = 0;
        for (int i = 0; i < a.length; i++) {
            double d = Math.abs(a[i] - b[i]);
            switch (distType) {
                case 0 -> sum += d * d;      // Euclidean
                case 1 -> sum += d;           // Manhattan
                case 2 -> sum = Math.max(sum, d); // Chebyshev
            }
        }
        return distType == 0 ? Math.sqrt(sum) : sum;
    }
}
