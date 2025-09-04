package com.knezevic.edaf.algorithm.cgp;

import com.knezevic.edaf.algorithm.cgp.model.ProgramGraph;
import com.knezevic.edaf.core.api.Individual;

import java.util.Arrays;

/**
 * Represents an individual in the Cartesian Genetic Programming algorithm.
 * It encapsulates the genotype (an integer array), the fitness, and the decoded phenotype (a program graph).
 */
public class CgpIndividual implements Individual<int[]> {

    private final int[] genotype;
    private double fitness = Double.NEGATIVE_INFINITY;
    private ProgramGraph phenotype;

    /**
     * Constructs a new CGP individual.
     *
     * @param genotype The integer array representing the genotype.
     */
    public CgpIndividual(int[] genotype) {
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

    /**
     * Gets the cached phenotype (program graph).
     *
     * @return The decoded program graph, or null if not yet decoded.
     */
    public ProgramGraph getPhenotype() {
        return phenotype;
    }

    /**
     * Sets the cached phenotype.
     *
     * @param phenotype The decoded program graph.
     */
    public void setPhenotype(ProgramGraph phenotype) {
        this.phenotype = phenotype;
    }

    @Override
    public Individual<int[]> copy() {
        CgpIndividual copy = new CgpIndividual(Arrays.copyOf(genotype, genotype.length));
        copy.setFitness(this.fitness);
        // The phenotype is not copied, it should be decoded again if needed.
        // This is because the phenotype can be large and is derived from the genotype.
        return copy;
    }

    @Override
    public String toString() {
        return "CgpIndividual{" +
                "fitness=" + fitness +
                ", genotype=" + Arrays.toString(genotype) +
                '}';
    }
}
