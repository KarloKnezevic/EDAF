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
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class GrammarBitStringRepresentation implements Representation<BitString> {

    private final GrammarTreeEngine treeEngine;
    private final int length;

    /**
     * Creates grammar bitstring representation.
     * @param params configuration the input value map
     */
    public GrammarBitStringRepresentation(Map<String, Object> params) {
        this.treeEngine = new GrammarTreeEngine(params);
        this.length = treeEngine.encoding().genomeLength();
    }

    /**
     * Returns representation type identifier.
     *
     * @return the type
     */
    @Override
    public String type() {
        return "grammar-bitstring";
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
        return new BitString(Arrays.copyOf(genotype.genes(), length));
    }

    /**
     * Returns compact value summary.
     *
     * @param genotype encoded genotype value
     * @return the summarize
     */
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
     * @return the tree engine
     */
    public GrammarTreeEngine treeEngine() {
        return treeEngine;
    }

    /**
     * Fixed genotype length in bits.
     * @return the computed length
     */
    public int length() {
        return length;
    }
}
