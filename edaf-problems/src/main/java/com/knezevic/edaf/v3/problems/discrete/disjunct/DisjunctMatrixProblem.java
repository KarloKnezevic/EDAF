/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.problems.discrete.disjunct;

import com.knezevic.edaf.v3.core.api.Fitness;
import com.knezevic.edaf.v3.core.api.ScalarFitness;
import com.knezevic.edaf.v3.repr.types.BitString;

/**
  * t-disjunct matrix design objective using exact paper fitness:.
 * {@code fit1(A) = sum_{S in S_t} delta(S)}.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class DisjunctMatrixProblem extends AbstractDisjunctMatrixProblem {

    /**
     * Creates a new DisjunctMatrixProblem instance.
     *
     * @param m matrix row count
     * @param n problem dimension
     * @param t disjunct level
     * @param evaluationConfig the evaluationConfig argument
     */
    public DisjunctMatrixProblem(int m, int n, int t, DisjunctEvaluationConfig evaluationConfig) {
        super("disjunct-matrix", m, n, t, evaluationConfig);
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
                ? DisjunctFitnessFunctions.fit1(matrix, t())
                : DisjunctFitnessFunctions.fit1Sampled(
                        matrix,
                        t(),
                        evaluationConfig().sampleSize(),
                        samplingSeed(genotype)
                );
        return new ScalarFitness(fit);
    }
}
