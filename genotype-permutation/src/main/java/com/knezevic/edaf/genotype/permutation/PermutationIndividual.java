package com.knezevic.edaf.genotype.permutation;

import java.util.Arrays;

import com.knezevic.edaf.core.api.Individual;

public class PermutationIndividual implements Individual<int[]> {
    private double fitness;
    private final int[] genotype;

    public PermutationIndividual(int[] genotype) {
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
        return new PermutationIndividual(genotype.clone());
    }

     @Override
    public String toString() {
        return "PermutationIndividual{" +
                "genotype=" + Arrays.toString(genotype) +
                ", fitness=" + fitness +
                '}';
    }
}
