package com.knezevic.edaf.examples.crypto;

/**
 * Calculates the nonlinearity of a boolean function.
 * Nonlinearity is a measure of how different a function is from the set of all affine functions.
 * It is calculated using the Walsh-Hadamard Transform.
 */
public class Nonlinearity implements FitnessCriterion {
    private final int n;

    public Nonlinearity(int n) {
        this.n = n;
    }

    @Override
    public double compute(int[] function) {
        int[] wtResult = new int[1 << n];
        int wtMax = CryptoUtils.walshHadamardTransform(function, n, wtResult);
        return ((1 << n) - wtMax) / 2.0;
    }
}
