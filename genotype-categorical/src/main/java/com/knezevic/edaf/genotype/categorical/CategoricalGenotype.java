package com.knezevic.edaf.genotype.categorical;

import com.knezevic.edaf.core.api.Genotype;
import java.util.Random;

/**
 * Factory for creating random categorical individuals.
 * Each position i has cardinalities[i] valid categories (0 to cardinalities[i]-1).
 */
public class CategoricalGenotype implements Genotype<int[]> {

    private final int[] cardinalities;
    private final Random random;

    public CategoricalGenotype(int[] cardinalities, Random random) {
        this.cardinalities = cardinalities;
        this.random = random;
    }

    @Override
    public int[] create() {
        int[] genotype = new int[cardinalities.length];
        for (int i = 0; i < cardinalities.length; i++) {
            genotype[i] = random.nextInt(cardinalities[i]);
        }
        return genotype;
    }

    @Override
    public int getLength() { return cardinalities.length; }

    public int[] getCardinalities() { return cardinalities; }
}
