package com.knezevic.edaf.algorithm.nes;

import java.util.Random;

/**
 * Maintains and updates the search distribution for Separable NES (SNES).
 * <p>
 * The search distribution is a diagonal Gaussian: N(mu, diag(sigma^2)).
 * Each generation samples from this distribution and updates its parameters
 * using the natural gradient of expected fitness.
 * </p>
 * <p>
 * Update rules (natural gradient on the exponential parameterization):
 * <pre>
 *   mu    ← mu    + eta_mu    * sigma ⊙ Σ(u_i * z_i)
 *   sigma ← sigma ⊙ exp(0.5 * eta_sigma * Σ(u_i * (z_i² - 1)))
 * </pre>
 * where z_i are standard normal samples and u_i are fitness utilities.
 * </p>
 *
 * @see <a href="https://arxiv.org/abs/1106.4487">Wierstra et al., "Natural Evolution Strategies"</a>
 */
public class NesStatistics {

    private final int dimension;
    private final Random random;
    private final double etaMu;
    private final double etaSigma;

    private double[] mu;
    private double[] sigma;

    /** Standard normal samples from the most recent sampleRaw() call. */
    private double[][] lastStandardSamples;

    /**
     * Creates a new SNES statistics instance.
     *
     * @param dimension problem dimensionality
     * @param random RNG for reproducibility
     * @param etaMu learning rate for the mean vector
     * @param etaSigma learning rate for the standard deviation vector
     */
    public NesStatistics(int dimension, Random random, double etaMu, double etaSigma) {
        this.dimension = dimension;
        this.random = random;
        this.etaMu = etaMu;
        this.etaSigma = etaSigma;
        this.mu = new double[dimension];
        this.sigma = new double[dimension];
        java.util.Arrays.fill(sigma, 1.0);
    }

    /**
     * Creates a new SNES statistics instance with custom initial mu and sigma.
     */
    public NesStatistics(int dimension, Random random, double etaMu, double etaSigma,
                         double[] initialMu, double[] initialSigma) {
        this(dimension, random, etaMu, etaSigma);
        System.arraycopy(initialMu, 0, this.mu, 0, dimension);
        System.arraycopy(initialSigma, 0, this.sigma, 0, dimension);
    }

    /**
     * Sample lambda individuals from the current distribution.
     * Returns actual genotype values: x_i = mu + sigma ⊙ z_i.
     * Also stores the standard normal samples z_i for use in {@link #update}.
     *
     * @param lambda number of individuals to sample
     * @return array of genotype vectors [lambda][dimension]
     */
    public double[][] sampleRaw(int lambda) {
        lastStandardSamples = new double[lambda][dimension];
        double[][] actual = new double[lambda][dimension];
        for (int i = 0; i < lambda; i++) {
            for (int j = 0; j < dimension; j++) {
                double z = random.nextGaussian();
                lastStandardSamples[i][j] = z;
                actual[i][j] = mu[j] + sigma[j] * z;
            }
        }
        return actual;
    }

    /**
     * Update mu and sigma using natural gradient and utility values.
     * <p>
     * Must be called after {@link #sampleRaw} with matching lambda.
     * The samples parameter is not used for the update (we use lastStandardSamples),
     * but is provided for API completeness.
     * </p>
     *
     * @param samples the genotype samples (unused; kept for API clarity)
     * @param utilities rank-based utility values aligned with sample indices
     */
    public void update(double[][] samples, double[] utilities) {
        int lambda = utilities.length;

        // Natural gradient for mu: Σ u_i * z_i
        double[] gradMu = new double[dimension];
        // Natural gradient for log(sigma): Σ u_i * (z_i² - 1)
        double[] gradSigma = new double[dimension];

        for (int i = 0; i < lambda; i++) {
            for (int j = 0; j < dimension; j++) {
                double z = lastStandardSamples[i][j];
                gradMu[j] += utilities[i] * z;
                gradSigma[j] += utilities[i] * (z * z - 1.0);
            }
        }

        // Apply updates
        for (int j = 0; j < dimension; j++) {
            mu[j] += etaMu * sigma[j] * gradMu[j];
            sigma[j] *= Math.exp(0.5 * etaSigma * gradSigma[j]);
            // Prevent sigma collapse
            sigma[j] = Math.max(1e-8, sigma[j]);
        }
    }

    public double[] getMu() {
        return mu.clone();
    }

    public double[] getSigma() {
        return sigma.clone();
    }

    public int getDimension() {
        return dimension;
    }
}
