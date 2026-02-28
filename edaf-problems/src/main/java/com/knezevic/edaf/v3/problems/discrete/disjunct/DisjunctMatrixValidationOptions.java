/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.problems.discrete.disjunct;

/**
  * Controls when validation uses exhaustive enumeration versus statistically.
 * justified sampling, and configures the associated confidence bound.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
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
      * Reasonable defaults for practical validation:.
     * exact for up to 200k subsets, otherwise sample via Hoeffding target.
     * @return the defaults
     */
    public static DisjunctMatrixValidationOptions defaults() {
        return new DisjunctMatrixValidationOptions(200_000L, 0L, 0.95, 0.02, 12345L);
    }

    /**
     * Executes max exact subsets.
     *
     * @return the computed max exact subsets
     */
    public long maxExactSubsets() {
        return maxExactSubsets;
    }

    /**
     * Executes sample size.
     *
     * @return the computed sample size
     */
    public long sampleSize() {
        return sampleSize;
    }

    /**
     * Executes confidence level.
     *
     * @return the computed confidence level
     */
    public double confidenceLevel() {
        return confidenceLevel;
    }

    /**
     * Executes absolute error.
     *
     * @return the computed absolute error
     */
    public double absoluteError() {
        return absoluteError;
    }

    /**
     * Executes random seed.
     *
     * @return the computed random seed
     */
    public long randomSeed() {
        return randomSeed;
    }

    /**
      * Resolves effective sample size. If explicit sample size is not supplied,.
     * it is derived from Hoeffding inequality:
     * {@code n >= ln(2/alpha)/(2*eps^2)} where {@code alpha = 1 - confidence}.
     * @return the computed resolved sample size
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
