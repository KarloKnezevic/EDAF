/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.repr.impl;

import com.knezevic.edaf.v3.core.api.Representation;
import com.knezevic.edaf.v3.core.rng.RngStream;
import com.knezevic.edaf.v3.repr.grammar.GrammarTreeEngine;
import com.knezevic.edaf.v3.repr.types.BitString;

import java.util.Arrays;
import java.util.Map;

/**
 * Fixed-length bitstring representation for grammar-encoded GP trees.
 *
 * <p>Bitstrings are decoded through BFS production choices, enabling direct compatibility
 * with existing binary/discrete EDA drivers.</p>
 */
public final class GrammarBitStringRepresentation implements Representation<BitString> {

    private final GrammarTreeEngine treeEngine;
    private final int length;

    /**
     * Creates grammar bitstring representation.
     */
    public GrammarBitStringRepresentation(Map<String, Object> params) {
        this.treeEngine = new GrammarTreeEngine(params);
        this.length = treeEngine.encoding().genomeLength();
    }

    @Override
    public String type() {
        return "grammar-bitstring";
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
        return new BitString(Arrays.copyOf(genotype.genes(), length));
    }

    @Override
    public String summarize(BitString genotype) {
        if (genotype == null) {
            return "null";
        }
        try {
            GrammarTreeEngine.TreeInspection inspection = treeEngine.inspect(genotype);
            return inspection.infix();
        } catch (Exception e) {
            String bits = genotype.toString();
            return bits.length() <= 64 ? bits : bits.substring(0, 61) + "...";
        }
    }

    /**
     * Returns associated grammar tree engine for advanced consumers.
     */
    public GrammarTreeEngine treeEngine() {
        return treeEngine;
    }

    /**
     * Fixed genotype length in bits.
     */
    public int length() {
        return length;
    }
}
