package com.knezevic.edaf.genotype.fp;

import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.core.impl.*;

import java.util.Arrays;

/**
 * Represents an individual with a floating-point genotype.
 */
public class FpIndividual implements Individual<double[]> {

    private final double[] genotype;
    private double fitness;

    public FpIndividual(double[] genotype) {
        this.genotype = genotype;
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

    @Override
    public Individual<double[]> copy() {
        FpIndividual copy = new FpIndividual(Arrays.copyOf(genotype, genotype.length));
        copy.setFitness(fitness);
        return copy;
    }

    @Override
    public String toString() {
        return "FpIndividual{" +
                "genotype=" + Arrays.toString(genotype) +
                ", fitness=" + fitness +
                '}';
    }
}
