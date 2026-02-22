package com.knezevic.edaf.v3.problems.discrete.disjunct;

import java.util.Random;

/**
 * Exact fitness functions defined in the paper:
 *
 * <ul>
 *     <li>{@code fit1(A) = sum_{S in S_t} delta(S)}</li>
 *     <li>{@code fit2(A) = |{S in S_t : delta(S) > f}|}</li>
 *     <li>{@code fit3(A) = fit1(A) / (C(N,t) * (N-t))}</li>
 * </ul>
 */
public final class DisjunctFitnessFunctions {

    private DisjunctFitnessFunctions() {
        // utility class
    }

    /**
     * Exact {@code fit1} for t-disjunct objective.
     */
    public static long fit1(DisjunctMatrix matrix, int t) {
        validateT(matrix, t);
        DisjunctDeviationEvaluator evaluator = new DisjunctDeviationEvaluator(matrix);
        long[] sum = new long[]{0L};
        DisjunctCombinatorics.forEachCombination(matrix.columns(), t, subset ->
                sum[0] += evaluator.deviationForSubset(subset));
        return sum[0];
    }

    /**
     * Exact {@code fit2} for (t,f)-resolvable objective.
     */
    public static long fit2(DisjunctMatrix matrix, int t, int f) {
        validateT(matrix, t);
        if (f < 0) {
            throw new IllegalArgumentException("f must be >= 0");
        }
        DisjunctDeviationEvaluator evaluator = new DisjunctDeviationEvaluator(matrix);
        long[] count = new long[]{0L};
        DisjunctCombinatorics.forEachCombination(matrix.columns(), t, subset -> {
            int deviation = evaluator.deviationForSubset(subset);
            if (deviation > f) {
                count[0]++;
            }
        });
        return count[0];
    }

    /**
     * Exact {@code fit3} for (t,epsilon)-disjunct objective.
     */
    public static double fit3(DisjunctMatrix matrix, int t) {
        validateT(matrix, t);
        long fit1 = fit1(matrix, t);
        double denominator = DisjunctCombinatorics.binomialCoefficientAsDouble(matrix.columns(), t)
                * (matrix.columns() - t);
        if (denominator <= 0.0) {
            return Double.NaN;
        }
        return fit1 / denominator;
    }

    /**
     * Sampled estimator of fit1:
     * {@code E[delta(S)] * C(N,t)} using uniformly sampled subsets.
     */
    public static double fit1Sampled(DisjunctMatrix matrix, int t, long sampleSize, long seed) {
        validateT(matrix, t);
        long effectiveSamples = Math.max(1L, sampleSize);
        DisjunctDeviationEvaluator evaluator = new DisjunctDeviationEvaluator(matrix);
        Random random = new Random(seed);
        int[] subset = new int[t];
        double totalDeviation = 0.0;
        for (long sample = 0L; sample < effectiveSamples; sample++) {
            DisjunctCombinatorics.sampleCombination(random, matrix.columns(), t, subset);
            totalDeviation += evaluator.deviationForSubset(subset);
        }
        double meanDeviation = totalDeviation / effectiveSamples;
        double combinations = DisjunctCombinatorics.binomialCoefficientAsDouble(matrix.columns(), t);
        return meanDeviation * combinations;
    }

    /**
     * Sampled estimator of fit2:
     * {@code P(delta(S) > f) * C(N,t)} using uniformly sampled subsets.
     */
    public static double fit2Sampled(DisjunctMatrix matrix, int t, int f, long sampleSize, long seed) {
        validateT(matrix, t);
        if (f < 0) {
            throw new IllegalArgumentException("f must be >= 0");
        }
        long effectiveSamples = Math.max(1L, sampleSize);
        DisjunctDeviationEvaluator evaluator = new DisjunctDeviationEvaluator(matrix);
        Random random = new Random(seed);
        int[] subset = new int[t];
        long violating = 0L;
        for (long sample = 0L; sample < effectiveSamples; sample++) {
            DisjunctCombinatorics.sampleCombination(random, matrix.columns(), t, subset);
            if (evaluator.deviationForSubset(subset) > f) {
                violating++;
            }
        }
        double violationRate = violating / (double) effectiveSamples;
        double combinations = DisjunctCombinatorics.binomialCoefficientAsDouble(matrix.columns(), t);
        return violationRate * combinations;
    }

    /**
     * Sampled estimator of fit3:
     * {@code E[delta(S)/(N-t)]} using uniformly sampled subsets.
     */
    public static double fit3Sampled(DisjunctMatrix matrix, int t, long sampleSize, long seed) {
        validateT(matrix, t);
        long effectiveSamples = Math.max(1L, sampleSize);
        int remaining = matrix.columns() - t;
        DisjunctDeviationEvaluator evaluator = new DisjunctDeviationEvaluator(matrix);
        Random random = new Random(seed);
        int[] subset = new int[t];
        double sum = 0.0;
        for (long sample = 0L; sample < effectiveSamples; sample++) {
            DisjunctCombinatorics.sampleCombination(random, matrix.columns(), t, subset);
            sum += evaluator.deviationForSubset(subset) / (double) remaining;
        }
        return sum / effectiveSamples;
    }

    private static void validateT(DisjunctMatrix matrix, int t) {
        if (t < 1) {
            throw new IllegalArgumentException("t must be >= 1");
        }
        if (t >= matrix.columns()) {
            throw new IllegalArgumentException("t must be < N (columns)");
        }
    }
}
