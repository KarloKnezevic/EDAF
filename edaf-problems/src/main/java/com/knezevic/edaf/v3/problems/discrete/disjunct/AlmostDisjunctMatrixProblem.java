/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.problems.discrete.disjunct;

import com.knezevic.edaf.v3.core.api.Fitness;
import com.knezevic.edaf.v3.core.api.ScalarFitness;
import com.knezevic.edaf.v3.repr.types.BitString;

/**
  * (t,epsilon)-disjunct matrix design objective using exact paper fitness:.
 * {@code fit3(A) = fit1(A)/(C(N,t)*(N-t))}.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class AlmostDisjunctMatrixProblem extends AbstractDisjunctMatrixProblem {

    private final double epsilon;

    /**
     * Creates a new AlmostDisjunctMatrixProblem instance.
     *
     * @param m matrix row count
     * @param n problem dimension
     * @param t disjunct level
     * @param epsilon approximation threshold
     * @param evaluationConfig the evaluationConfig argument
     */
    public AlmostDisjunctMatrixProblem(int m, int n, int t, double epsilon, DisjunctEvaluationConfig evaluationConfig) {
        super("almost-disjunct-matrix", m, n, t, evaluationConfig);
        if (epsilon < 0.0 || epsilon > 1.0) {
            throw new IllegalArgumentException("epsilon must be in [0,1]");
        }
        this.epsilon = epsilon;
    }

    /**
     * Evaluates candidate solution.
     *
     * @param genotype candidate genotype
     * @return fitness value
     */
    @Override
    public Fitness evaluate(BitString genotype) {
        DisjunctMatrix matrix = matrixFrom(genotype);
        long subsets = totalSubsets();
        DisjunctEvaluationMode mode = evaluationConfig().resolve(subsets);
        double fit = mode == DisjunctEvaluationMode.EXACT
                ? DisjunctFitnessFunctions.fit3(matrix, t())
                : DisjunctFitnessFunctions.fit3Sampled(
                        matrix,
                        t(),
                        evaluationConfig().sampleSize(),
                        samplingSeed(genotype)
                );
        return new ScalarFitness(fit);
    }

    /**
     * Exposes configured epsilon threshold for reporting/diagnostics.
     * @return the computed epsilon
     */
    public double epsilon() {
        return epsilon;
    }
}
