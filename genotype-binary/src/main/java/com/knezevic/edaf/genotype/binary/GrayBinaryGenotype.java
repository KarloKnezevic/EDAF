package com.knezevic.edaf.genotype.binary;

import java.util.Random;

/**
 * A factory for creating Gray-coded binary genotypes.
 */
public class GrayBinaryGenotype extends BinaryGenotype {

    public GrayBinaryGenotype(int length, Random random) {
        super(length, random);
    }

    @Override
    public byte[] create() {
        byte[] binary = super.create();
        return toGray(binary);
    }

    /**
     * Converts a binary genotype to Gray code.
     *
     * @param binary The binary genotype.
     * @return The Gray-coded genotype.
     */
    public static byte[] toGray(byte[] binary) {
        byte[] gray = new byte[binary.length];
        gray[0] = binary[0];
        for (int i = 1; i < binary.length; i++) {
            gray[i] = (byte) (binary[i - 1] ^ binary[i]);
        }
        return gray;
    }

    /**
     * Converts a Gray-coded genotype to binary.
     *
     * @param gray The Gray-coded genotype.
     * @return The binary genotype.
     */
    public static byte[] fromGray(byte[] gray) {
        byte[] binary = new byte[gray.length];
        binary[0] = gray[0];
        for (int i = 1; i < gray.length; i++) {
            binary[i] = (byte) (binary[i - 1] ^ gray[i]);
        }
        return binary;
    }
}
