/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.problems.discrete.disjunct;

import com.knezevic.edaf.v3.core.api.ObjectiveSense;
import com.knezevic.edaf.v3.core.api.Problem;
import com.knezevic.edaf.v3.repr.types.BitString;

import java.util.List;

/**
 * Base problem contract for disjunct-matrix family.
 *
 * <p>Genotype is a single bitstring interpreted as an {@code M x N} binary matrix
 * in column-major order (each consecutive block of {@code M} bits is one column).</p>
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
abstract class AbstractDisjunctMatrixProblem implements Problem<BitString> {

    private final String name;
    private final int m;
    private final int n;
    private final int t;
    private final int expectedLength;
    private final DisjunctEvaluationConfig evaluationConfig;

    /**
     * Creates a new AbstractDisjunctMatrixProblem instance.
     *
     * @param name identifier name
     * @param m matrix row count
     * @param n problem dimension
     * @param t disjunct level
     * @param evaluationConfig the evaluationConfig argument
     */
    protected AbstractDisjunctMatrixProblem(String name, int m, int n, int t, DisjunctEvaluationConfig evaluationConfig) {
        if (m <= 0) {
            throw new IllegalArgumentException("M must be > 0");
        }
        if (n <= 1) {
            throw new IllegalArgumentException("N must be > 1");
        }
        if (t < 1 || t >= n) {
            throw new IllegalArgumentException("t must satisfy 1 <= t < N");
        }
        this.name = name;
        this.m = m;
        this.n = n;
        this.t = t;
        this.expectedLength = Math.multiplyExact(m, n);
        this.evaluationConfig = evaluationConfig == null
                ? DisjunctEvaluationConfig.defaults()
                : evaluationConfig;
    }

    /**
     * Returns problem identifier.
     *
     * @return problem identifier
     */
    @Override
    public final String name() {
        return name;
    }

    /**
     * Returns objective optimization sense.
     *
     * @return objective sense
     */
    @Override
    public final ObjectiveSense objectiveSense() {
        return ObjectiveSense.MINIMIZE;
    }

    /**
     * Returns feasibility violations.
     *
     * @param genotype candidate genotype
     * @return violation message list
     */
    @Override
    public List<String> violations(BitString genotype) {
        if (genotype == null) {
            return List.of("genotype must not be null");
        }
        if (genotype.length() != expectedLength) {
            return List.of("bitstring length must equal M*N = " + expectedLength + ", got " + genotype.length());
        }
        return List.of();
    }

    /**
     * Executes matrix from.
     *
     * @param genotype candidate genotype
     * @return the matrix from
     */
    protected final DisjunctMatrix matrixFrom(BitString genotype) {
        if (genotype == null) {
            throw new IllegalArgumentException("genotype must not be null");
        }
        if (genotype.length() != expectedLength) {
            throw new IllegalArgumentException(
                    "bitstring length must equal M*N = " + expectedLength + ", got " + genotype.length()
            );
        }
        return DisjunctMatrix.fromBitString(genotype, m, n);
    }

    /**
     * Executes m.
     *
     * @return the computed m
     */
    protected final int m() {
        return m;
    }

    /**
     * Executes n.
     *
     * @return the computed n
     */
    protected final int n() {
        return n;
    }

    /**
     * Executes t.
     *
     * @return the computed t
     */
    protected final int t() {
        return t;
    }

    /**
     * Executes evaluation config.
     *
     * @return the evaluation config
     */
    protected final DisjunctEvaluationConfig evaluationConfig() {
        return evaluationConfig;
    }

    /**
     * Converts to tal subsets.
     *
     * @return the tal subsets representation
     */
    protected final long totalSubsets() {
        return DisjunctCombinatorics.binomialCoefficientCapped(n, t, Long.MAX_VALUE - 1L);
    }

    /**
     * Executes sampling seed.
     *
     * @param genotype candidate genotype
     * @return the computed sampling seed
     */
    protected final long samplingSeed(BitString genotype) {
        return evaluationConfig.samplingSeed() ^ fnv1a64(genotype.genes());
    }

    private static long fnv1a64(boolean[] genes) {
        long hash = 0xcbf29ce484222325L;
        for (boolean gene : genes) {
            hash ^= gene ? 0x9E3779B97F4A7C15L : 0xD6E8FEB86659FD93L;
            hash *= 0x100000001b3L;
        }
        return hash;
    }
}
