/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.core.metrics;

import com.knezevic.edaf.v3.core.api.Individual;
import com.knezevic.edaf.v3.core.api.Population;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Population-level scalar metrics used by telemetry, reporting and adaptive policies.
 *
 * <p>Implemented statistics:
 * <ul>
 *     <li>{@code best}: best scalar fitness value in population</li>
 *     <li>{@code mean}: arithmetic mean of scalar fitness</li>
 *     <li>{@code std}: population standard deviation of scalar fitness</li>
 *     <li>{@code diversity}: ratio of unique genotype string projections</li>
 *     <li>{@code entropy}: Shannon entropy over genotype-frequency histogram</li>
 * </ul>
 *
 * <p>For entropy, if genotype summary frequencies are {@code p_k}, then:
 * <pre>
 *   H = - Σ_k p_k log2(p_k)
 * </pre></p>
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class PopulationMetrics {

    private PopulationMetrics() {
        // utility class
    }

    /**
     * Returns the best scalar fitness in the population.
     * @param population evaluated population snapshot
     * @return smallest or largest scalar fitness according to objective sense
     */
    public static <G> double best(Population<G> population) {
        return population.best().fitness().scalar();
    }

    /**
     * Returns arithmetic mean of scalar fitness values.
     * @param population evaluated population snapshot
     * @return arithmetic mean of scalar fitness values
     */
    public static <G> double mean(Population<G> population) {
        double sum = 0.0;
        for (Individual<G> individual : population) {
            sum += individual.fitness().scalar();
        }
        return sum / Math.max(1, population.size());
    }

    /**
     * Returns standard deviation of scalar fitness values.
     * @param population evaluated population snapshot
     * @return population standard deviation of scalar fitness values
     */
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
     * @param population evaluated population snapshot
     * @return ratio of unique genotype projections in {@code population}
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
     * @param population evaluated population snapshot
     * @return Shannon entropy over genotype-frequency histogram
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
