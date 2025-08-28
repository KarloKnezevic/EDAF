package hr.fer.zemris.edaf.genotype.integer;

import hr.fer.zemris.edaf.core.Genotype;

import java.util.Random;

/**
 * A factory for creating integer genotypes.
 */
public class IntegerGenotype implements Genotype<int[]> {

    private final int length;
    private final int min;
    private final int max;
    private final Random random;

    public IntegerGenotype(int length, int min, int max, Random random) {
        this.length = length;
        this.min = min;
        this.max = max;
        this.random = random;
    }

    @Override
    public int[] create() {
        int[] genotype = new int[length];
        for (int i = 0; i < length; i++) {
            genotype[i] = random.nextInt(max - min + 1) + min;
        }
        return genotype;
    }

    @Override
    public int getLength() {
        return length;
    }
}
