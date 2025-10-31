package com.knezevic.edaf.testing.problems.crypto;

public class Nonlinearity implements FitnessCriterion {
    private final int n;
    public Nonlinearity(int n) { this.n = n; }
    @Override
    public double compute(int[] function) {
        int[] wtResult = new int[1 << n];
        int wtMax = CryptoUtils.walshHadamardTransform(function, n, wtResult);
        return ((1 << n) - wtMax) / 2.0;
    }
}


