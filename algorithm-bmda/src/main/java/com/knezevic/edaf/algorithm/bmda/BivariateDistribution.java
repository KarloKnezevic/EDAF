package com.knezevic.edaf.algorithm.bmda;

/**
 * Represents the bivariate probability distribution for a pair of variables.
 */
public class BivariateDistribution {
    // A 2x2 matrix to store the bivariate probabilities.
    // probabilities[i][j] = P(X=i, Y=j)
    private final double[][] probabilities = new double[2][2];

    /**
     * Updates the distribution with a new observation.
     *
     * @param val1 The value of the first variable.
     * @param val2 The value of the second variable.
     */
    public void update(int val1, int val2) {
        probabilities[val1][val2]++;
    }

    /**
     * Normalizes the distribution.
     *
     * @param total The total number of observations.
     */
    public void normalize(int total) {
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                probabilities[i][j] /= total;
            }
        }
    }

    /**
     * Returns the probability of a given pair of values.
     *
     * @param val1 The value of the first variable.
     * @param val2 The value of the second variable.
     * @return The probability of the given pair of values.
     */
    public double getProbability(int val1, int val2) {
        return probabilities[val1][val2];
    }
}
