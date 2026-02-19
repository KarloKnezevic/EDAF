package com.knezevic.edaf.genotype.realvalued;

import com.knezevic.edaf.core.api.Genotype;

import java.util.Arrays;
import java.util.Random;

/**
 * Factory for creating random real-valued individuals with initial strategy parameters.
 */
public class RealValuedGenotype implements Genotype<double[]> {

    private final int length;
    private final double lowerBound;
    private final double upperBound;
    private final double initialSigma;
    private final Random random;

    public RealValuedGenotype(int length, double lowerBound, double upperBound,
                              double initialSigma, Random random) {
        this.length = length;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.initialSigma = initialSigma;
        this.random = random;
    }

    @Override
    public double[] create() {
        double[] genotype = new double[length];
        for (int i = 0; i < length; i++) {
            genotype[i] = lowerBound + (upperBound - lowerBound) * random.nextDouble();
        }
        return genotype;
    }

    public RealValuedIndividual createIndividual() {
        double[] genotype = create();
        double[] sigmas = new double[length];
        Arrays.fill(sigmas, initialSigma);
        return new RealValuedIndividual(genotype, sigmas);
    }

    @Override
    public int getLength() {
        return length;
    }

    public double getLowerBound() {
        return lowerBound;
    }

    public double getUpperBound() {
        return upperBound;
    }

    public double getInitialSigma() {
        return initialSigma;
    }
}
