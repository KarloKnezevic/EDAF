package com.knezevic.edaf.examples.crypto;

/**
 * Calculates the balancedness of a boolean function.
 * A function is balanced if its truth table contains an equal number of 0s and 1s.
 */
public class Balancedness implements FitnessCriterion {
    private final int n;

    public Balancedness(int n) {
        this.n = n;
    }

    @Override
    public double compute(int[] function) {
        int zeros = 0;
        for (int i = 0; i < (1 << n); i++) {
            if (function[i] == 0) {
                zeros++;
            }
        }

        int half = (1 << n) / 2;
        if (zeros == half) {
            return 1; // Perfect score for balanced functions
        } else {
            // Penalize unbalanced functions. The further from balanced, the higher the penalty.
            return -Math.abs(zeros - half);
        }
    }
}
