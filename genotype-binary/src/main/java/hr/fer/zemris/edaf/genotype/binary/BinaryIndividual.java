package hr.fer.zemris.edaf.genotype.binary;

import hr.fer.zemris.edaf.core.Individual;

import java.util.Arrays;

/**
 * Represents an individual with a binary genotype.
 */
public class BinaryIndividual implements Individual<byte[]> {

    private final byte[] genotype;
    private double fitness;

    public BinaryIndividual(byte[] genotype) {
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
    public byte[] getGenotype() {
        return genotype;
    }

    @Override
    public Individual<byte[]> copy() {
        BinaryIndividual copy = new BinaryIndividual(Arrays.copyOf(genotype, genotype.length));
        copy.setFitness(fitness);
        return copy;
    }

    @Override
    public String toString() {
        return "BinaryIndividual{" +
                "genotype=" + Arrays.toString(genotype) +
                ", fitness=" + fitness +
                '}';
    }
}
