package com.knezevic.edaf.v3.problems.discrete.disjunct;

import com.knezevic.edaf.v3.repr.types.BitString;

import java.util.Arrays;
import java.util.Random;

/**
 * Validator for formal DM/RM/ADM properties from the paper.
 *
 * <p>For small instances, validation is exhaustive over all {@code C(N,t)} subsets.
 * For larger instances, validation switches to random subset sampling with a
 * Hoeffding-style confidence bound.</p>
 */
public final class DisjunctMatrixValidator {

    private static final double EPSILON_TOLERANCE = 1.0e-12;

    private DisjunctMatrixValidator() {
        // utility class
    }

    /**
     * Validates t-disjunct property:
     * for every t-subset S and every remaining column x_j, supp(x_j) is not subset
     * of union supports of S.
     */
    public static DisjunctMatrixValidationResult validateDisjunct(DisjunctMatrix matrix,
                                                                  int t,
                                                                  DisjunctMatrixValidationOptions options) {
        validateCommon(matrix, t);
        return validate(
                "t-disjunct",
                matrix,
                t,
                optionsOrDefault(options),
                (deviation, remainingColumns) -> deviation > 0,
                "delta(S) must be 0 for every subset S"
        );
    }

    /**
     * Bitstring convenience overload for t-disjunct validation.
     */
    public static DisjunctMatrixValidationResult validateDisjunct(BitString genotype,
                                                                  int m,
                                                                  int n,
                                                                  int t,
                                                                  DisjunctMatrixValidationOptions options) {
        return validateDisjunct(DisjunctMatrix.fromBitString(genotype, m, n), t, options);
    }

    /**
     * Validates (t,f)-resolvable property:
     * for every t-subset S, delta(S) <= f.
     */
    public static DisjunctMatrixValidationResult validateResolvable(DisjunctMatrix matrix,
                                                                    int t,
                                                                    int f,
                                                                    DisjunctMatrixValidationOptions options) {
        validateCommon(matrix, t);
        if (f < 0) {
            throw new IllegalArgumentException("f must be >= 0");
        }
        return validate(
                "(t,f)-resolvable",
                matrix,
                t,
                optionsOrDefault(options),
                (deviation, remainingColumns) -> deviation > f,
                "delta(S) must be <= f for every subset S"
        );
    }

    /**
     * Bitstring convenience overload for (t,f)-resolvable validation.
     */
    public static DisjunctMatrixValidationResult validateResolvable(BitString genotype,
                                                                    int m,
                                                                    int n,
                                                                    int t,
                                                                    int f,
                                                                    DisjunctMatrixValidationOptions options) {
        return validateResolvable(DisjunctMatrix.fromBitString(genotype, m, n), t, f, options);
    }

    /**
     * Validates (t,epsilon)-disjunct (ADM) property:
     * for every t-subset S, delta(S)/(N-t) <= epsilon.
     */
    public static DisjunctMatrixValidationResult validateAlmostDisjunct(DisjunctMatrix matrix,
                                                                        int t,
                                                                        double epsilon,
                                                                        DisjunctMatrixValidationOptions options) {
        validateCommon(matrix, t);
        if (epsilon < 0.0 || epsilon > 1.0) {
            throw new IllegalArgumentException("epsilon must be in [0,1]");
        }
        return validate(
                "(t,epsilon)-disjunct",
                matrix,
                t,
                optionsOrDefault(options),
                (deviation, remainingColumns) ->
                        deviation / (double) remainingColumns > epsilon + EPSILON_TOLERANCE,
                "delta(S)/(N-t) must be <= epsilon for every subset S"
        );
    }

    /**
     * Bitstring convenience overload for (t,epsilon)-disjunct validation.
     */
    public static DisjunctMatrixValidationResult validateAlmostDisjunct(BitString genotype,
                                                                        int m,
                                                                        int n,
                                                                        int t,
                                                                        double epsilon,
                                                                        DisjunctMatrixValidationOptions options) {
        return validateAlmostDisjunct(DisjunctMatrix.fromBitString(genotype, m, n), t, epsilon, options);
    }

    private static DisjunctMatrixValidationResult validate(String definition,
                                                           DisjunctMatrix matrix,
                                                           int t,
                                                           DisjunctMatrixValidationOptions options,
                                                           ViolationPredicate violationPredicate,
                                                           String requirementDescription) {
        long totalSubsets = DisjunctCombinatorics.binomialCoefficientCapped(
                matrix.columns(),
                t,
                Long.MAX_VALUE - 1L
        );
        long maxExact = options.maxExactSubsets();
        if (totalSubsets <= maxExact) {
            return validateExact(definition, matrix, t, totalSubsets, violationPredicate, requirementDescription);
        }
        return validateSampled(definition, matrix, t, totalSubsets, options, violationPredicate, requirementDescription);
    }

    private static DisjunctMatrixValidationResult validateExact(String definition,
                                                                DisjunctMatrix matrix,
                                                                int t,
                                                                long totalSubsets,
                                                                ViolationPredicate violationPredicate,
                                                                String requirementDescription) {
        int remainingColumns = matrix.columns() - t;
        DisjunctDeviationEvaluator evaluator = new DisjunctDeviationEvaluator(matrix);
        long[] evaluated = new long[]{0L};
        long[] violations = new long[]{0L};
        int[][] witnessSubset = new int[1][];
        int[] witnessDeviation = new int[]{-1};

        DisjunctCombinatorics.forEachCombination(matrix.columns(), t, subset -> {
            evaluated[0]++;
            int deviation = evaluator.deviationForSubset(subset);
            if (violationPredicate.isViolation(deviation, remainingColumns)) {
                violations[0]++;
                if (witnessSubset[0] == null) {
                    witnessSubset[0] = Arrays.copyOf(subset, subset.length);
                    witnessDeviation[0] = deviation;
                }
            }
        });

        boolean valid = violations[0] == 0L;
        double rate = evaluated[0] == 0L ? 0.0 : violations[0] / (double) evaluated[0];
        String message = valid
                ? "Exact validation passed: " + requirementDescription + "."
                : "Exact validation failed: violating subset found.";

        return new DisjunctMatrixValidationResult(
                definition,
                DisjunctMatrixValidationMode.EXACT,
                valid,
                true,
                evaluated[0],
                totalSubsets,
                violations[0],
                rate,
                1.0,
                0.0,
                rate,
                witnessDeviation[0],
                witnessSubset[0],
                message
        );
    }

    private static DisjunctMatrixValidationResult validateSampled(String definition,
                                                                  DisjunctMatrix matrix,
                                                                  int t,
                                                                  long totalSubsets,
                                                                  DisjunctMatrixValidationOptions options,
                                                                  ViolationPredicate violationPredicate,
                                                                  String requirementDescription) {
        int remainingColumns = matrix.columns() - t;
        long sampleSize = options.resolvedSampleSize();
        if (totalSubsets > 0 && totalSubsets < sampleSize) {
            sampleSize = totalSubsets;
        }

        DisjunctDeviationEvaluator evaluator = new DisjunctDeviationEvaluator(matrix);
        Random random = new Random(options.randomSeed());
        int[] subset = new int[t];
        long violations = 0L;
        int[] witnessSubset = null;
        int witnessDeviation = -1;

        for (long sample = 0L; sample < sampleSize; sample++) {
            DisjunctCombinatorics.sampleCombination(random, matrix.columns(), t, subset);
            int deviation = evaluator.deviationForSubset(subset);
            if (violationPredicate.isViolation(deviation, remainingColumns)) {
                violations++;
                if (witnessSubset == null) {
                    witnessSubset = Arrays.copyOf(subset, subset.length);
                    witnessDeviation = deviation;
                }
            }
        }

        double estimatedRate = sampleSize == 0L ? 0.0 : violations / (double) sampleSize;
        double errorBound = hoeffdingBound(options.confidenceLevel(), sampleSize);
        double upperBound = Math.min(1.0, estimatedRate + errorBound);
        boolean valid = violations == 0L;

        String message;
        if (!valid) {
            message = "Sampled validation found a violating subset, so the property does not hold.";
        } else {
            message = "No violating subset observed in sampled mode. "
                    + "With confidence " + options.confidenceLevel()
                    + ", violation rate is <= " + upperBound
                    + ". Requirement checked: " + requirementDescription + ".";
        }

        return new DisjunctMatrixValidationResult(
                definition,
                DisjunctMatrixValidationMode.SAMPLED,
                valid,
                false,
                sampleSize,
                totalSubsets,
                violations,
                estimatedRate,
                options.confidenceLevel(),
                errorBound,
                upperBound,
                witnessDeviation,
                witnessSubset,
                message
        );
    }

    private static void validateCommon(DisjunctMatrix matrix, int t) {
        if (matrix == null) {
            throw new IllegalArgumentException("matrix must not be null");
        }
        if (t < 1) {
            throw new IllegalArgumentException("t must be >= 1");
        }
        if (t >= matrix.columns()) {
            throw new IllegalArgumentException("t must be < N (columns)");
        }
    }

    private static DisjunctMatrixValidationOptions optionsOrDefault(DisjunctMatrixValidationOptions options) {
        return options == null ? DisjunctMatrixValidationOptions.defaults() : options;
    }

    private static double hoeffdingBound(double confidenceLevel, long sampleSize) {
        if (sampleSize <= 0L) {
            return 1.0;
        }
        double alpha = 1.0 - confidenceLevel;
        return Math.sqrt(Math.log(2.0 / alpha) / (2.0 * sampleSize));
    }

    @FunctionalInterface
    private interface ViolationPredicate {
        boolean isViolation(int deviation, int remainingColumns);
    }
}
