package com.knezevic.edaf.v3.repr.impl;

import com.knezevic.edaf.v3.core.api.Representation;
import com.knezevic.edaf.v3.core.rng.RngStream;
import com.knezevic.edaf.v3.repr.types.BitString;

import java.util.Arrays;

/**
 * Bitstring representation implementation.
 */
public final class BitStringRepresentation implements Representation<BitString> {

    private final int length;

    public BitStringRepresentation(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("length must be > 0");
        }
        this.length = length;
    }

    @Override
    public String type() {
        return "bitstring";
    }

    @Override
    public BitString random(RngStream rng) {
        boolean[] genes = new boolean[length];
        for (int i = 0; i < genes.length; i++) {
            genes[i] = rng.nextDouble() < 0.5;
        }
        return new BitString(genes);
    }

    @Override
    public boolean isValid(BitString genotype) {
        return genotype != null && genotype.length() == length;
    }

    @Override
    public BitString repair(BitString genotype) {
        if (genotype == null) {
            return new BitString(new boolean[length]);
        }
        boolean[] repaired = Arrays.copyOf(genotype.genes(), length);
        return new BitString(repaired);
    }

    @Override
    public String summarize(BitString genotype) {
        String asString = genotype.toString();
        if (asString.length() <= 64) {
            return asString;
        }
        return asString.substring(0, 61) + "...";
    }
}
