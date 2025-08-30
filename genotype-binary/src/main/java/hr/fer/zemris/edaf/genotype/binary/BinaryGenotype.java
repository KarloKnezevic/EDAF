package hr.fer.zemris.edaf.genotype.binary;

import hr.fer.zemris.edaf.core.api.*;
import hr.fer.zemris.edaf.core.impl.*;

import java.util.Random;

/**
 * A factory for creating binary genotypes.
 */
public class BinaryGenotype implements Genotype<byte[]> {

    private final int length;
    private final Random random;

    public BinaryGenotype(int length, Random random) {
        this.length = length;
        this.random = random;
    }

    @Override
    public byte[] create() {
        byte[] genotype = new byte[length];
        for (int i = 0; i < length; i++) {
            genotype[i] = (byte) random.nextInt(2);
        }
        return genotype;
    }

    @Override
    public int getLength() {
        return length;
    }
}
