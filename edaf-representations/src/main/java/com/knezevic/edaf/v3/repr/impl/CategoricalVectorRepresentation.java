/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.repr.impl;

import com.knezevic.edaf.v3.core.api.Representation;
import com.knezevic.edaf.v3.core.rng.RngStream;
import com.knezevic.edaf.v3.repr.types.CategoricalVector;

import java.util.Arrays;
import java.util.List;

/**
 * Categorical vector representation with per-position symbol draw.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class CategoricalVectorRepresentation implements Representation<CategoricalVector> {

    private final int length;
    private final List<String> symbols;

    /**
     * Creates a new CategoricalVectorRepresentation instance.
     *
     * @param length the length argument
     * @param symbols the symbols argument
     */
    public CategoricalVectorRepresentation(int length, List<String> symbols) {
        if (length <= 0) {
            throw new IllegalArgumentException("length must be > 0");
        }
        if (symbols == null || symbols.isEmpty()) {
            throw new IllegalArgumentException("symbols must not be empty");
        }
        this.length = length;
        this.symbols = List.copyOf(symbols);
    }

    /**
     * Returns representation type identifier.
     *
     * @return the type
     */
    @Override
    public String type() {
        return "categorical-vector";
    }

    /**
     * Samples a random value in representation domain.
     *
     * @param rng random stream
     * @return the random
     */
    @Override
    public CategoricalVector random(RngStream rng) {
        String[] values = new String[length];
        for (int i = 0; i < length; i++) {
            values[i] = symbols.get(rng.nextInt(symbols.size()));
        }
        return new CategoricalVector(values);
    }

    /**
     * Returns whether value is valid in representation domain.
     *
     * @param genotype encoded genotype value
     * @return true if valid; otherwise false
     */
    @Override
    public boolean isValid(CategoricalVector genotype) {
        if (genotype == null || genotype.length() != length) {
            return false;
        }
        for (String value : genotype.categories()) {
            if (!symbols.contains(value)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Repairs value to representation domain constraints.
     *
     * @param genotype encoded genotype value
     * @return the repair
     */
    @Override
    public CategoricalVector repair(CategoricalVector genotype) {
        if (genotype == null) {
            String[] fallback = new String[length];
            Arrays.fill(fallback, symbols.get(0));
            return new CategoricalVector(fallback);
        }
        String[] repaired = Arrays.copyOf(genotype.categories(), length);
        for (int i = 0; i < repaired.length; i++) {
            if (!symbols.contains(repaired[i])) {
                repaired[i] = symbols.get(0);
            }
        }
        return new CategoricalVector(repaired);
    }

    /**
     * Returns compact value summary.
     *
     * @param genotype encoded genotype value
     * @return the summarize
     */
    @Override
    public String summarize(CategoricalVector genotype) {
        return genotype.toString();
    }
}
