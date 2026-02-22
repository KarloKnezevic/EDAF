package com.knezevic.edaf.v3.problems.discrete.disjunct;

import java.math.BigInteger;
import java.util.Random;
import java.util.function.Consumer;

/**
 * Combinatorial helpers used for exhaustive and sampled validation/evaluation
 * of disjunct matrix properties.
 */
public final class DisjunctCombinatorics {

    private DisjunctCombinatorics() {
        // utility class
    }

    /**
     * Iterates all {@code k}-combinations of indices {@code [0, n-1]} in lexicographic order.
     */
    public static void forEachCombination(int n, int k, Consumer<int[]> visitor) {
        if (k < 0 || k > n) {
            throw new IllegalArgumentException("invalid combination parameters: n=" + n + ", k=" + k);
        }
        if (k == 0) {
            visitor.accept(new int[0]);
            return;
        }

        int[] combination = new int[k];
        for (int i = 0; i < k; i++) {
            combination[i] = i;
        }

        while (true) {
            visitor.accept(combination);

            int position = k - 1;
            while (position >= 0 && combination[position] == n - k + position) {
                position--;
            }
            if (position < 0) {
                return;
            }
            combination[position]++;
            for (int i = position + 1; i < k; i++) {
                combination[i] = combination[i - 1] + 1;
            }
        }
    }

    /**
     * Samples one {@code k}-combination uniformly without replacement into {@code output}.
     * Output is sorted increasingly.
     */
    public static void sampleCombination(Random random, int n, int k, int[] output) {
        if (output.length != k) {
            throw new IllegalArgumentException("output length must equal k");
        }
        if (k < 0 || k > n) {
            throw new IllegalArgumentException("invalid combination parameters: n=" + n + ", k=" + k);
        }

        int remainingToPick = k;
        int out = 0;
        for (int index = 0; index < n && remainingToPick > 0; index++) {
            double pickProbability = remainingToPick / (double) (n - index);
            if (random.nextDouble() < pickProbability) {
                output[out++] = index;
                remainingToPick--;
            }
        }
    }

    /**
     * Returns {@code n choose k} capped to {@code cap + 1} to avoid overflow.
     */
    public static long binomialCoefficientCapped(int n, int k, long cap) {
        if (k < 0 || k > n) {
            return 0L;
        }
        if (k == 0 || k == n) {
            return 1L;
        }
        BigInteger capValue = BigInteger.valueOf(cap);
        BigInteger result = BigInteger.ONE;
        int effectiveK = Math.min(k, n - k);
        for (int i = 1; i <= effectiveK; i++) {
            result = result.multiply(BigInteger.valueOf(n - effectiveK + i))
                    .divide(BigInteger.valueOf(i));
            if (result.compareTo(capValue) > 0) {
                return cap + 1L;
            }
        }
        return result.longValueExact();
    }

    /**
     * Returns {@code n choose k} as double for ratio-based metrics.
     */
    public static double binomialCoefficientAsDouble(int n, int k) {
        if (k < 0 || k > n) {
            return 0.0;
        }
        if (k == 0 || k == n) {
            return 1.0;
        }
        int effectiveK = Math.min(k, n - k);
        BigInteger result = BigInteger.ONE;
        for (int i = 1; i <= effectiveK; i++) {
            result = result.multiply(BigInteger.valueOf(n - effectiveK + i))
                    .divide(BigInteger.valueOf(i));
        }
        return result.doubleValue();
    }
}
