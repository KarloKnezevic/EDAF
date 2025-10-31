package com.knezevic.edaf.testing.problems.crypto;

public class AlgebraicDegree implements FitnessCriterion {
    private final int n;
    public AlgebraicDegree(int n) { this.n = n; }
    @Override
    public double compute(int[] function) {
        // Placeholder simple heuristic: favor higher number of ones and transitions
        int transitions = 0;
        for (int i = 1; i < function.length; i++) if (function[i] != function[i-1]) transitions++;
        return transitions;
    }
}


