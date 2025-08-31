package com.knezevic.edaf.genotype.integer;

import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.core.impl.*;

import java.util.Arrays;

/**
 * Represents an individual with an integer genotype.
 */
public class IntegerIndividual implements Individual<int[]> {

    private final int[] genotype;
    private double fitness;

    public IntegerIndividual(int[] genotype) {
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
    public int[] getGenotype() {
        return genotype;
    }

    @Override
    public Individual<int[]> copy() {
        IntegerIndividual copy = new IntegerIndividual(Arrays.copyOf(genotype, genotype.length));
        copy.setFitness(fitness);
        return copy;
    }

    @Override
    public String toString() {
        return "IntegerIndividual{" +
                "genotype=" + Arrays.toString(genotype) +
                ", fitness=" + fitness +
                '}';
    }
}
