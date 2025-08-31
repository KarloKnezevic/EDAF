package com.knezevic.edaf.genotype.fp.mutation;

import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.core.impl.*;
import com.knezevic.edaf.genotype.fp.FpIndividual;

import java.util.Random;

/**
 * Polynomial mutation for floating-point individuals.
 */
public class PolynomialMutation implements Mutation<FpIndividual> {

    private final Random random;
    private final double mutationProbability;
    private final double distributionIndex;
    private final double lowerBound;
    private final double upperBound;

    public PolynomialMutation(Random random, double mutationProbability, double distributionIndex, double lowerBound, double upperBound) {
        this.random = random;
        this.mutationProbability = mutationProbability;
        this.distributionIndex = distributionIndex;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    @Override
    public void mutate(FpIndividual individual) {
        double[] genotype = individual.getGenotype();
        for (int i = 0; i < genotype.length; i++) {
            if (random.nextDouble() < mutationProbability) {
                double y = genotype[i];
                double delta1 = (y - lowerBound) / (upperBound - lowerBound);
                double delta2 = (upperBound - y) / (upperBound - lowerBound);
                double mutPow = 1.0 / (distributionIndex + 1.0);
                double deltaq;

                double u = random.nextDouble();
                if (u <= 0.5) {
                    double xy = 1.0 - delta1;
                    double val = 2.0 * u + (1.0 - 2.0 * u) * Math.pow(xy, distributionIndex + 1.0);
                    deltaq = Math.pow(val, mutPow) - 1.0;
                } else {
                    double xy = 1.0 - delta2;
                    double val = 2.0 * (1.0 - u) + 2.0 * (u - 0.5) * Math.pow(xy, distributionIndex + 1.0);
                    deltaq = 1.0 - Math.pow(val, mutPow);
                }

                y = y + deltaq * (upperBound - lowerBound);

                if (y < lowerBound) y = lowerBound;
                if (y > upperBound) y = upperBound;

                genotype[i] = y;
            }
        }
    }
}
