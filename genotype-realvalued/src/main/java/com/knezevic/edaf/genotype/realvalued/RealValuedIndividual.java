package com.knezevic.edaf.genotype.realvalued;

import com.knezevic.edaf.core.api.Individual;

import java.util.Arrays;

/**
 * Individual with real-valued object variables and self-adaptive strategy parameters (sigmas).
 * Each gene has an associated step size (sigma) for self-adaptive mutation.
 */
public class RealValuedIndividual implements Individual<double[]> {

    private final double[] genotype;
    private final double[] sigmas;
    private double fitness;

    public RealValuedIndividual(double[] genotype, double[] sigmas) {
        this.genotype = genotype;
        this.sigmas = sigmas;
    }

    @Override
    public double getFitness() {
        return fitness;
    }

    @Override
    public void setFitness(double fitness) {
        this.fitness = fitness;
    }

    @Override
    public double[] getGenotype() {
        return genotype;
    }

    public double[] getSigmas() {
        return sigmas;
    }

    @Override
    public RealValuedIndividual copy() {
        RealValuedIndividual copy = new RealValuedIndividual(
                Arrays.copyOf(genotype, genotype.length),
                Arrays.copyOf(sigmas, sigmas.length));
        copy.setFitness(fitness);
        return copy;
    }

    @Override
    public String toString() {
        return "RealValuedIndividual{" +
                "genotype=" + Arrays.toString(genotype) +
                ", sigmas=" + Arrays.toString(sigmas) +
                ", fitness=" + fitness +
                '}';
    }
}
