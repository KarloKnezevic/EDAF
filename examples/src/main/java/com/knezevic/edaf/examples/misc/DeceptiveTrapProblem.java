package com.knezevic.edaf.examples.misc;

import com.knezevic.edaf.core.api.Individual;
import com.knezevic.edaf.core.api.Problem;
import com.knezevic.edaf.genotype.binary.BinaryIndividual;

/**
 * Implements a Deceptive Trap Function problem.
 * This problem is constructed by concatenating multiple k-bit "trap" functions.
 * For each k-bit block, the fitness is k if all bits are 1 (global optimum),
 * otherwise it is (k - 1 - u), where u is the number of 1s in the block.
 * This creates a "deceptive" local optimum at all 0s.
 * The total fitness is the sum of the fitness of all blocks.
 *
 * We seek to maximize this value, so we return its negative as the fitness
 * for the framework's minimizer.
 */
public class DeceptiveTrapProblem implements Problem<BinaryIndividual> {

    private final int k; // The size of each trap block

    public DeceptiveTrapProblem() {
        // Default to a 4-bit trap function.
        // This can be overridden by a constructor if configured from YAML.
        this.k = 4;
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

        // Framework minimizes, so we negate the value we want to maximize.
        individual.setFitness(-totalFitness);
    }
}
