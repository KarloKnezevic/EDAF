package com.knezevic.edaf.testing.problems.crypto;

public class Balancedness implements FitnessCriterion {
    private final int n;
    public Balancedness(int n) { this.n = n; }
    @Override
    public double compute(int[] function) {
        int ones = 0;
        for (int v : function) if (v == 1) ones++;
        int target = (1 << n) / 2;
        return -Math.abs(ones - target);
    }
}


