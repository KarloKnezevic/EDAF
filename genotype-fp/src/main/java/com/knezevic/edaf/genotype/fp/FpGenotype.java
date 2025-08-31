package com.knezevic.edaf.genotype.fp;

import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.core.impl.*;

import java.util.Random;

/**
 * A factory for creating floating-point genotypes.
 */
public class FpGenotype implements Genotype<double[]> {

    private final int length;
    private final double min;
    private final double max;
    private final Random random;

    public FpGenotype(int length, double min, double max, Random random) {
        this.length = length;
        this.min = min;
        this.max = max;
        this.random = random;
    }

    @Override
    public double[] create() {
        double[] genotype = new double[length];
        for (int i = 0; i < length; i++) {
            genotype[i] = min + (max - min) * random.nextDouble();
        }
        return genotype;
    }

    @Override
    public int getLength() {
        return length;
    }
}
