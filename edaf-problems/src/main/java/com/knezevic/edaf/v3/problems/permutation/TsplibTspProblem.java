/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.problems.permutation;

import com.knezevic.edaf.v3.core.api.Fitness;
import com.knezevic.edaf.v3.core.api.ObjectiveSense;
import com.knezevic.edaf.v3.core.api.Problem;
import com.knezevic.edaf.v3.core.api.ScalarFitness;
import com.knezevic.edaf.v3.repr.types.PermutationVector;

import java.util.List;

/**
 * TSP problem backed by TSPLIB coordinates.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class TsplibTspProblem implements Problem<PermutationVector> {

    private final String instanceName;
    private final double[][] coordinates;

    /**
     * Creates a new TsplibTspProblem instance.
     *
     * @param instanceName the instanceName argument
     * @param coordinates the coordinates argument
     */
    public TsplibTspProblem(String instanceName, double[][] coordinates) {
        this.instanceName = instanceName;
        this.coordinates = coordinates;
    }

    /**
     * Returns problem identifier.
     *
     * @return problem identifier
     */
    @Override
    public String name() {
        return "tsplib-" + instanceName;
    }

    /**
     * Returns objective optimization sense.
     *
     * @return objective sense
     */
    @Override
    public ObjectiveSense objectiveSense() {
        return ObjectiveSense.MINIMIZE;
    }

    /**
     * Evaluates candidate solution.
     *
     * @param genotype candidate genotype
     * @return fitness value
     */
    @Override
    public Fitness evaluate(PermutationVector genotype) {
        int[] order = genotype.order();
        double length = 0.0;
        for (int i = 0; i < order.length; i++) {
            int from = order[i];
            int to = order[(i + 1) % order.length];
            length += distance(coordinates[from], coordinates[to]);
        }
        return new ScalarFitness(length);
    }

    /**
     * Returns feasibility violations.
     *
     * @param genotype candidate genotype
     * @return violation message list
     */
    @Override
    public List<String> violations(PermutationVector genotype) {
        if (genotype.size() != coordinates.length) {
            return List.of("Permutation size must equal TSPLIB city count " + coordinates.length);
        }
        return List.of();
    }

    /**
     * Executes city count.
     *
     * @return the computed city count
     */
    public int cityCount() {
        return coordinates.length;
    }

    private static double distance(double[] a, double[] b) {
        double dx = a[0] - b[0];
        double dy = a[1] - b[1];
        return Math.rint(Math.sqrt(dx * dx + dy * dy));
    }
}
