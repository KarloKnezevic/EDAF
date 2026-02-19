package com.knezevic.edaf.v3.core.metrics;

import com.knezevic.edaf.v3.core.api.Individual;
import com.knezevic.edaf.v3.core.api.Population;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Utility methods for common population metrics used by console output and persistence.
 */
public final class PopulationMetrics {

    private PopulationMetrics() {
        // utility class
    }

    public static <G> double best(Population<G> population) {
        return population.best().fitness().scalar();
    }

    public static <G> double mean(Population<G> population) {
        double sum = 0.0;
        for (Individual<G> individual : population) {
            sum += individual.fitness().scalar();
        }
        return sum / Math.max(1, population.size());
    }

    public static <G> double std(Population<G> population) {
        double mean = mean(population);
        double sum = 0.0;
        for (Individual<G> individual : population) {
            double diff = individual.fitness().scalar() - mean;
            sum += diff * diff;
        }
        return Math.sqrt(sum / Math.max(1, population.size()));
    }

    /**
     * Diversity measured as fraction of unique genotype summaries.
     */
    public static <G> double diversity(Population<G> population) {
        if (population.size() == 0) {
            return 0.0;
        }
        Set<String> unique = new HashSet<>();
        for (Individual<G> individual : population) {
            unique.add(String.valueOf(individual.genotype()));
        }
        return unique.size() / (double) population.size();
    }

    /**
     * Shannon entropy over genotype-summary frequencies.
     */
    public static <G> double entropy(Population<G> population) {
        if (population.size() == 0) {
            return 0.0;
        }
        Map<String, Integer> freq = new HashMap<>();
        for (Individual<G> individual : population) {
            freq.merge(String.valueOf(individual.genotype()), 1, Integer::sum);
        }
        double entropy = 0.0;
        for (int count : freq.values()) {
            double p = count / (double) population.size();
            entropy -= p * (Math.log(p) / Math.log(2));
        }
        return entropy;
    }
}
