/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.repr.impl;

import com.knezevic.edaf.v3.core.api.Representation;
import com.knezevic.edaf.v3.core.rng.RngStream;
import com.knezevic.edaf.v3.repr.types.VariableLengthVector;

import java.util.ArrayList;
import java.util.List;

/**
 * Minimal variable-length integer token representation scaffold.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class VariableLengthVectorRepresentation implements Representation<VariableLengthVector<Integer>> {

    private final int minLength;
    private final int maxLength;
    private final int maxToken;

    /**
     * Creates a new VariableLengthVectorRepresentation instance.
     *
     * @param minLength the minLength argument
     * @param maxLength the maxLength argument
     * @param maxToken the maxToken argument
     */
    public VariableLengthVectorRepresentation(int minLength, int maxLength, int maxToken) {
        if (minLength < 1 || maxLength < minLength) {
            throw new IllegalArgumentException("invalid length bounds");
        }
        this.minLength = minLength;
        this.maxLength = maxLength;
        this.maxToken = Math.max(1, maxToken);
    }

    /**
     * Returns representation type identifier.
     *
     * @return the type
     */
    @Override
    public String type() {
        return "variable-length-vector";
    }

    /**
     * Samples a random value in representation domain.
     *
     * @param rng random stream
     * @return the random
     */
    @Override
    public VariableLengthVector<Integer> random(RngStream rng) {
        int length = minLength + rng.nextInt(maxLength - minLength + 1);
        List<Integer> values = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            values.add(rng.nextInt(maxToken));
        }
        return new VariableLengthVector<>(values);
    }

    /**
     * Returns whether value is valid in representation domain.
     *
     * @param genotype encoded genotype value
     * @return true if valid; otherwise false
     */
    @Override
    public boolean isValid(VariableLengthVector<Integer> genotype) {
        if (genotype == null || genotype.size() < minLength || genotype.size() > maxLength) {
            return false;
        }
        for (Integer value : genotype.values()) {
            if (value == null || value < 0 || value >= maxToken) {
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
    public VariableLengthVector<Integer> repair(VariableLengthVector<Integer> genotype) {
        if (genotype == null || genotype.values().isEmpty()) {
            return new VariableLengthVector<>(List.of(0));
        }
        List<Integer> repaired = new ArrayList<>(genotype.values());
        while (repaired.size() < minLength) {
            repaired.add(0);
        }
        while (repaired.size() > maxLength) {
            repaired.remove(repaired.size() - 1);
        }
        for (int i = 0; i < repaired.size(); i++) {
            int value = repaired.get(i) == null ? 0 : repaired.get(i);
            repaired.set(i, Math.floorMod(value, maxToken));
        }
        return new VariableLengthVector<>(repaired);
    }

    /**
     * Returns compact value summary.
     *
     * @param genotype encoded genotype value
     * @return the summarize
     */
    @Override
    public String summarize(VariableLengthVector<Integer> genotype) {
        return genotype.toString();
    }
}
