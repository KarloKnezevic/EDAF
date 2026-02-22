package com.knezevic.edaf.v3.problems.discrete.disjunct;

/**
 * Controls when validation uses exhaustive enumeration versus statistically
 * justified sampling, and configures the associated confidence bound.
 */
public final class DisjunctMatrixValidationOptions {

    private final long maxExactSubsets;
    private final long sampleSize;
    private final double confidenceLevel;
    private final double absoluteError;
    private final long randomSeed;

    /**
     * Creates custom options.
     *
     * @param maxExactSubsets max number of t-subsets for exact mode.
     * @param sampleSize explicit sample size; use 0 to derive from confidence/error.
     * @param confidenceLevel confidence in (0,1), e.g. 0.95.
     * @param absoluteError target Hoeffding half-width, e.g. 0.02.
     * @param randomSeed sampling seed for deterministic reproducibility.
     */
    public DisjunctMatrixValidationOptions(long maxExactSubsets,
                                           long sampleSize,
                                           double confidenceLevel,
                                           double absoluteError,
                                           long randomSeed) {
        if (maxExactSubsets < 1L) {
            throw new IllegalArgumentException("maxExactSubsets must be >= 1");
        }
        if (sampleSize < 0L) {
            throw new IllegalArgumentException("sampleSize must be >= 0");
        }
        if (!(confidenceLevel > 0.0 && confidenceLevel < 1.0)) {
            throw new IllegalArgumentException("confidenceLevel must be in (0,1)");
        }
        if (!(absoluteError > 0.0 && absoluteError < 1.0)) {
            throw new IllegalArgumentException("absoluteError must be in (0,1)");
        }
        this.maxExactSubsets = maxExactSubsets;
        this.sampleSize = sampleSize;
        this.confidenceLevel = confidenceLevel;
        this.absoluteError = absoluteError;
        this.randomSeed = randomSeed;
    }

    /**
     * Reasonable defaults for practical validation:
     * exact for up to 200k subsets, otherwise sample via Hoeffding target.
     */
    public static DisjunctMatrixValidationOptions defaults() {
        return new DisjunctMatrixValidationOptions(200_000L, 0L, 0.95, 0.02, 12345L);
    }

    public long maxExactSubsets() {
        return maxExactSubsets;
    }

    public long sampleSize() {
        return sampleSize;
    }

    public double confidenceLevel() {
        return confidenceLevel;
    }

    public double absoluteError() {
        return absoluteError;
    }

    public long randomSeed() {
        return randomSeed;
    }

    /**
     * Resolves effective sample size. If explicit sample size is not supplied,
     * it is derived from Hoeffding inequality:
     * {@code n >= ln(2/alpha)/(2*eps^2)} where {@code alpha = 1 - confidence}.
     */
    public long resolvedSampleSize() {
        if (sampleSize > 0L) {
            return sampleSize;
        }
        double alpha = 1.0 - confidenceLevel;
        double n = Math.log(2.0 / alpha) / (2.0 * absoluteError * absoluteError);
        return Math.max(256L, (long) Math.ceil(n));
    }
}
