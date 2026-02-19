package com.knezevic.edaf.v3.repr.impl;

import com.knezevic.edaf.v3.core.api.Representation;
import com.knezevic.edaf.v3.core.rng.RngStream;
import com.knezevic.edaf.v3.repr.types.VariableLengthVector;

import java.util.ArrayList;
import java.util.List;

/**
 * Minimal variable-length integer token representation scaffold.
 */
public final class VariableLengthVectorRepresentation implements Representation<VariableLengthVector<Integer>> {

    private final int minLength;
    private final int maxLength;
    private final int maxToken;

    public VariableLengthVectorRepresentation(int minLength, int maxLength, int maxToken) {
        if (minLength < 1 || maxLength < minLength) {
            throw new IllegalArgumentException("invalid length bounds");
        }
        this.minLength = minLength;
        this.maxLength = maxLength;
        this.maxToken = Math.max(1, maxToken);
    }

    @Override
    public String type() {
        return "variable-length-vector";
    }

    @Override
    public VariableLengthVector<Integer> random(RngStream rng) {
        int length = minLength + rng.nextInt(maxLength - minLength + 1);
        List<Integer> values = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            values.add(rng.nextInt(maxToken));
        }
        return new VariableLengthVector<>(values);
    }

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

    @Override
    public String summarize(VariableLengthVector<Integer> genotype) {
        return genotype.toString();
    }
}
