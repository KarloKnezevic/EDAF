/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.problems.crypto;

import java.util.Arrays;

/**
 * Cached statistics for one boolean function truth table.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class BooleanFunctionStats {

    private final int n;
    private final int size;
    private final int[] truthTable;
    private final int ones;

    private Integer maxWalshAbs;
    private Double nonlinearity;
    private Integer algebraicDegree;

    private BooleanFunctionStats(int n, int[] truthTable) {
        this.n = n;
        this.truthTable = truthTable;
        this.size = truthTable.length;

        int countOnes = 0;
        for (int bit : truthTable) {
            if (bit == 1) {
                countOnes++;
            }
        }
        this.ones = countOnes;
    }

    /**
     * Builds immutable stats object from binary truth table values {0,1}.
     * @param n problem dimension
     * @param truthTable boolean function truth table
     * @return the of
     */
    public static BooleanFunctionStats of(int n, int[] truthTable) {
        if (n < 1 || n > 16) {
            throw new IllegalArgumentException("n must be in [1,16], got " + n);
        }
        int expected = 1 << n;
        if (truthTable.length != expected) {
            throw new IllegalArgumentException("Truth table length must be " + expected + ", got " + truthTable.length);
        }
        int[] copy = Arrays.copyOf(truthTable, truthTable.length);
        for (int bit : copy) {
            if (bit != 0 && bit != 1) {
                throw new IllegalArgumentException("Truth table values must be 0/1");
            }
        }
        return new BooleanFunctionStats(n, copy);
    }

    /**
     * Executes n.
     *
     * @return the computed n
     */
    public int n() {
        return n;
    }

    /**
     * Executes size.
     *
     * @return the number of elements
     */
    public int size() {
        return size;
    }

    /**
     * Executes ones.
     *
     * @return the computed ones
     */
    public int ones() {
        return ones;
    }

    /**
     * Executes zeros.
     *
     * @return the computed zeros
     */
    public int zeros() {
        return size - ones;
    }

    /**
     * Executes max walsh abs.
     *
     * @return the computed max walsh abs
     */
    public int maxWalshAbs() {
        if (maxWalshAbs == null) {
            int[] transformed = new int[size];
            for (int i = 0; i < size; i++) {
                transformed[i] = truthTable[i] == 0 ? 1 : -1;
            }

            for (int len = 1; len < size; len <<= 1) {
                for (int i = 0; i < size; i += len << 1) {
                    for (int j = 0; j < len; j++) {
                        int u = transformed[i + j];
                        int v = transformed[i + j + len];
                        transformed[i + j] = u + v;
                        transformed[i + j + len] = u - v;
                    }
                }
            }

            int max = 0;
            for (int value : transformed) {
                int abs = Math.abs(value);
                if (abs > max) {
                    max = abs;
                }
            }
            maxWalshAbs = max;
        }
        return maxWalshAbs;
    }

    /**
     * Executes nonlinearity.
     *
     * @return the computed nonlinearity
     */
    public double nonlinearity() {
        if (nonlinearity == null) {
            nonlinearity = (size - maxWalshAbs()) / 2.0;
        }
        return nonlinearity;
    }

    /**
     * Executes algebraic degree.
     *
     * @return the computed algebraic degree
     */
    public int algebraicDegree() {
        if (algebraicDegree == null) {
            int[] anf = Arrays.copyOf(truthTable, truthTable.length);
            for (int bit = 0; bit < n; bit++) {
                for (int mask = 0; mask < size; mask++) {
                    if ((mask & (1 << bit)) != 0) {
                        anf[mask] ^= anf[mask ^ (1 << bit)];
                    }
                }
            }

            int degree = 0;
            for (int mask = 0; mask < size; mask++) {
                if (anf[mask] == 1) {
                    degree = Math.max(degree, Integer.bitCount(mask));
                }
            }
            algebraicDegree = degree;
        }
        return algebraicDegree;
    }

    /**
     * Returns known upper bound for nonlinearity.
     * @return the computed nonlinearity upper bound
     */
    public double nonlinearityUpperBound() {
        if ((n & 1) == 0) {
            return (1 << (n - 1)) - (1 << (n / 2 - 1));
        }
        return (1 << (n - 1)) - (1 << ((n - 1) / 2));
    }
}
