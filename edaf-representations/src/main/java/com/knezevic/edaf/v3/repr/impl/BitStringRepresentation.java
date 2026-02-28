/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.repr.impl;

import com.knezevic.edaf.v3.core.api.Representation;
import com.knezevic.edaf.v3.core.rng.RngStream;
import com.knezevic.edaf.v3.repr.types.BitString;

import java.util.Arrays;

/**
 * Bitstring representation implementation.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class BitStringRepresentation implements Representation<BitString> {

    private final int length;

    /**
     * Creates a new BitStringRepresentation instance.
     *
     * @param length the length argument
     */
    public BitStringRepresentation(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("length must be > 0");
        }
        this.length = length;
    }

    /**
     * Returns representation type identifier.
     *
     * @return the type
     */
    @Override
    public String type() {
        return "bitstring";
    }

    /**
     * Samples a random value in representation domain.
     *
     * @param rng random stream
     * @return the random
     */
    @Override
    public BitString random(RngStream rng) {
        boolean[] genes = new boolean[length];
        for (int i = 0; i < genes.length; i++) {
            genes[i] = rng.nextDouble() < 0.5;
        }
        return new BitString(genes);
    }

    /**
     * Returns whether value is valid in representation domain.
     *
     * @param genotype encoded genotype value
     * @return true if valid; otherwise false
     */
    @Override
    public boolean isValid(BitString genotype) {
        return genotype != null && genotype.length() == length;
    }

    /**
     * Repairs value to representation domain constraints.
     *
     * @param genotype encoded genotype value
     * @return the repair
     */
    @Override
    public BitString repair(BitString genotype) {
        if (genotype == null) {
            return new BitString(new boolean[length]);
        }
        boolean[] repaired = Arrays.copyOf(genotype.genes(), length);
        return new BitString(repaired);
    }

    /**
     * Returns compact value summary.
     *
     * @param genotype encoded genotype value
     * @return the summarize
     */
    @Override
    public String summarize(BitString genotype) {
        String asString = genotype.toString();
        if (asString.length() <= 64) {
            return asString;
        }
        return asString.substring(0, 61) + "...";
    }
}
