package com.knezevic.edaf.algorithm.ltga;

import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.genotype.binary.BinaryIndividual;

import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Gene-Pool Optimal Mixing (GPOM) crossover for LTGA.
 */
public class GpomCrossover implements Crossover<BinaryIndividual> {

    private final List<Set<Integer>> tree;
    private final Random random;

    public GpomCrossover(List<Set<Integer>> tree, Random random) {
        this.tree = tree;
        this.random = random;
    }

    @Override
    public BinaryIndividual crossover(BinaryIndividual parent1, BinaryIndividual parent2) {
        byte[] p1 = parent1.getGenotype();
        byte[] p2 = parent2.getGenotype();
        byte[] offspring = new byte[p1.length];
        System.arraycopy(p1, 0, offspring, 0, p1.length);

        // Select a random subset of nodes from the linkage tree
        int i = random.nextInt(tree.size());
        Set<Integer> cluster = tree.get(i);

        // Copy genes from the second parent
        for (int geneIndex : cluster) {
            offspring[geneIndex] = p2[geneIndex];
        }

        return new BinaryIndividual(offspring);
    }
}
