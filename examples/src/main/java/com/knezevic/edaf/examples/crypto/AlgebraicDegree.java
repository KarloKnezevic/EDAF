package com.knezevic.edaf.examples.crypto;

/**
 * Calculates the algebraic degree of a boolean function.
 * The algebraic degree is the degree of the highest degree monomial in the
 * Algebraic Normal Form (ANF) of the function.
 */
public class AlgebraicDegree implements FitnessCriterion {
    private final int n;

    public AlgebraicDegree(int n) {
        this.n = n;
    }

    @Override
    public double compute(int[] function) {
        int[] anfResult = new int[1 << n];
        CryptoUtils.algebraicNormalForm(function, n, anfResult);

        int degree = 0;
        for (int i = 1; i < (1 << n); ++i) {
            if (anfResult[i] != 0) {
                int weight = CryptoUtils.hammingWeight(i);
                if (weight > degree) {
                    degree = weight;
                }
            }
        }
        return degree;
    }
}
