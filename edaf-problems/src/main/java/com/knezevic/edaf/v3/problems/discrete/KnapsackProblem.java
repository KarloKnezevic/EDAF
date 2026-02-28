/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.problems.discrete;

import com.knezevic.edaf.v3.core.api.Fitness;
import com.knezevic.edaf.v3.core.api.ObjectiveSense;
import com.knezevic.edaf.v3.core.api.Problem;
import com.knezevic.edaf.v3.core.api.ScalarFitness;
import com.knezevic.edaf.v3.repr.types.BitString;

import java.util.List;

/**
 * 0/1 knapsack benchmark with linear overweight penalty.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class KnapsackProblem implements Problem<BitString> {

    private final int[] weights;
    private final int[] values;
    private final int capacity;
    private final double penaltyPerUnit;

    /**
     * Creates a new KnapsackProblem instance.
     *
     * @param weights the weights argument
     * @param values the values argument
     * @param capacity the capacity argument
     * @param penaltyPerUnit the penaltyPerUnit argument
     */
    public KnapsackProblem(int[] weights, int[] values, int capacity, double penaltyPerUnit) {
        if (weights.length != values.length) {
            throw new IllegalArgumentException("weights and values must have equal length");
        }
        if (weights.length == 0) {
            throw new IllegalArgumentException("knapsack item set must not be empty");
        }
        this.weights = java.util.Arrays.copyOf(weights, weights.length);
        this.values = java.util.Arrays.copyOf(values, values.length);
        this.capacity = capacity;
        this.penaltyPerUnit = Math.max(0.0, penaltyPerUnit);
    }

    /**
     * Returns problem identifier.
     *
     * @return problem identifier
     */
    @Override
    public String name() {
        return "knapsack";
    }

    /**
     * Returns objective optimization sense.
     *
     * @return objective sense
     */
    @Override
    public ObjectiveSense objectiveSense() {
        return ObjectiveSense.MAXIMIZE;
    }

    /**
     * Evaluates candidate solution.
     *
     * @param genotype candidate genotype
     * @return fitness value
     */
    @Override
    public Fitness evaluate(BitString genotype) {
        int length = Math.min(genotype.length(), weights.length);
        int totalWeight = 0;
        int totalValue = 0;
        boolean[] genes = genotype.genes();

        for (int i = 0; i < length; i++) {
            if (genes[i]) {
                totalWeight += weights[i];
                totalValue += values[i];
            }
        }

        int overweight = Math.max(0, totalWeight - capacity);
        double penalized = totalValue - penaltyPerUnit * overweight;
        return new ScalarFitness(penalized);
    }

    /**
     * Returns feasibility violations.
     *
     * @param genotype candidate genotype
     * @return violation message list
     */
    @Override
    public List<String> violations(BitString genotype) {
        if (genotype.length() != weights.length) {
            return List.of("Bitstring length must equal item count " + weights.length);
        }
        return List.of();
    }

    /**
     * Executes item count.
     *
     * @return the computed item count
     */
    public int itemCount() {
        return weights.length;
    }

    /**
     * Executes capacity.
     *
     * @return the computed capacity
     */
    public int capacity() {
        return capacity;
    }
}
