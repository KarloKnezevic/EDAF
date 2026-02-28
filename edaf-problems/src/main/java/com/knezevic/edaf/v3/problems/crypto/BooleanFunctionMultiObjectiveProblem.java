/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.problems.crypto;

import com.knezevic.edaf.v3.core.api.Fitness;
import com.knezevic.edaf.v3.repr.types.BitString;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Multi-objective boolean-function problem emitting one objective per selected criterion.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class BooleanFunctionMultiObjectiveProblem extends AbstractBooleanFunctionProblem<BitString> {

    private final double[] scalarWeights;

    public BooleanFunctionMultiObjectiveProblem(int n,
                                                List<String> criteria,
                                                Map<String, Double> criterionWeights,
                                                double[] scalarWeights) {
        super(n, criteria, criterionWeights);
        this.scalarWeights = normalizeScalarWeights(scalarWeights, objectiveCount());
    }

    /**
     * Returns problem identifier.
     *
     * @return problem identifier
     */
    @Override
    public String name() {
        return "boolean-function-mo";
    }

    /**
     * Evaluates candidate solution.
     *
     * @param genotype candidate genotype
     * @return fitness value
     */
    @Override
    public Fitness evaluate(BitString genotype) {
        return evaluateVectorFitness(toTruthTableFromBits(genotype.genes()), scalarWeights);
    }

    /**
     * Returns feasibility violations.
     *
     * @param genotype candidate genotype
     * @return violation message list
     */
    @Override
    public List<String> violations(BitString genotype) {
        if (genotype.length() != truthTableSize) {
            return List.of("Bitstring length must be 2^n = " + truthTableSize);
        }
        return List.of();
    }

    private static double[] normalizeScalarWeights(double[] raw, int objectiveCount) {
        if (raw == null || raw.length == 0) {
            double[] defaults = new double[objectiveCount];
            Arrays.fill(defaults, 1.0 / objectiveCount);
            return defaults;
        }

        double[] out = new double[objectiveCount];
        double sum = 0.0;
        for (int i = 0; i < objectiveCount; i++) {
            double w = i < raw.length ? Math.max(0.0, raw[i]) : 0.0;
            out[i] = w;
            sum += w;
        }

        if (sum <= 0.0) {
            Arrays.fill(out, 1.0 / objectiveCount);
            return out;
        }

        for (int i = 0; i < out.length; i++) {
            out[i] /= sum;
        }
        return out;
    }
}
