package com.knezevic.edaf.v3.repr.impl;

import com.knezevic.edaf.v3.core.api.Representation;
import com.knezevic.edaf.v3.core.rng.RngStream;
import com.knezevic.edaf.v3.repr.types.MixedDiscreteVector;

import java.util.Arrays;

/**
 * Mixed discrete representation modeled as integer-coded attributes with cardinalities.
 */
public final class MixedDiscreteVectorRepresentation implements Representation<MixedDiscreteVector> {

    private final int[] cardinalities;

    public MixedDiscreteVectorRepresentation(int[] cardinalities) {
        if (cardinalities == null || cardinalities.length == 0) {
            throw new IllegalArgumentException("cardinalities must not be empty");
        }
        this.cardinalities = Arrays.copyOf(cardinalities, cardinalities.length);
    }

    @Override
    public String type() {
        return "mixed-discrete-vector";
    }

    @Override
    public MixedDiscreteVector random(RngStream rng) {
        int[] values = new int[cardinalities.length];
        for (int i = 0; i < cardinalities.length; i++) {
            int card = Math.max(1, cardinalities[i]);
            values[i] = rng.nextInt(card);
        }
        return new MixedDiscreteVector(values);
    }

    @Override
    public boolean isValid(MixedDiscreteVector genotype) {
        if (genotype == null || genotype.length() != cardinalities.length) {
            return false;
        }
        int[] values = genotype.encodedValues();
        for (int i = 0; i < values.length; i++) {
            if (values[i] < 0 || values[i] >= Math.max(1, cardinalities[i])) {
                return false;
            }
        }
        return true;
    }

    @Override
    public MixedDiscreteVector repair(MixedDiscreteVector genotype) {
        if (genotype == null) {
            return new MixedDiscreteVector(new int[cardinalities.length]);
        }
        int[] repaired = Arrays.copyOf(genotype.encodedValues(), cardinalities.length);
        for (int i = 0; i < repaired.length; i++) {
            int card = Math.max(1, cardinalities[i]);
            if (repaired[i] < 0 || repaired[i] >= card) {
                repaired[i] = Math.floorMod(repaired[i], card);
            }
        }
        return new MixedDiscreteVector(repaired);
    }

    @Override
    public String summarize(MixedDiscreteVector genotype) {
        return genotype.toString();
    }
}
