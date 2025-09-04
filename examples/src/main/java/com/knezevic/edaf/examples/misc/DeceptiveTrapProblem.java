package com.knezevic.edaf.examples.misc;

import com.knezevic.edaf.core.api.OptimizationType;
import com.knezevic.edaf.core.impl.AbstractProblem;
import com.knezevic.edaf.genotype.binary.BinaryIndividual;

import java.util.Map;

/**
 * Implements a Deceptive Trap Function problem.
 * This problem is constructed by concatenating multiple k-bit "trap" functions.
 * For each k-bit block, the fitness is k if all bits are 1 (global optimum),
 * otherwise it is (k - 1 - u), where u is the number of 1s in the block.
 * This creates a "deceptive" local optimum at all 0s.
 * The total fitness is the sum of the fitness of all blocks.
 * This is a maximization problem.
 */
public class DeceptiveTrapProblem extends AbstractProblem<BinaryIndividual> {

    private final int k; // The size of each trap block

    public DeceptiveTrapProblem(Map<String, Object> params) {
        super(params);
        // This can be overridden by a constructor if configured from YAML.
        this.k = (int) params.getOrDefault("k", 4);
    }

    @Override
    public void evaluate(BinaryIndividual individual) {
        byte[] genotype = individual.getGenotype();
        if (genotype.length % k != 0) {
            throw new IllegalArgumentException("Genotype length must be a multiple of k=" + k);
        }

        double totalFitness = 0;
        int numBlocks = genotype.length / k;

        for (int i = 0; i < numBlocks; i++) {
            int u = 0; // Count of 1s in the current block
            for (int j = 0; j < k; j++) {
                if (genotype[i * k + j] == 1) {
                    u++;
                }
            }

            if (u == k) {
                totalFitness += k; // Global optimum for the block
            } else {
                totalFitness += (k - 1 - u); // Deceptive trap
            }
        }

        individual.setFitness(totalFitness);
    }
}
