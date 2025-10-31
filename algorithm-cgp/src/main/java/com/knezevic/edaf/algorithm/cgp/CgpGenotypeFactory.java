package com.knezevic.edaf.algorithm.cgp;

import com.knezevic.edaf.core.runtime.RandomSource;
import com.knezevic.edaf.genotype.tree.primitives.Function;

import java.util.List;
import java.util.random.RandomGenerator;

/**
 * A factory for creating valid random CGP genotypes.
 */
public class CgpGenotypeFactory {

    private final CgpConfig config;
    private final List<Function> functionSet;
    private final int numInputs;
    private final int numOutputs;
    private final RandomGenerator random;
    private final int functionArity;
    private final int numNodes;
    private final int genesPerNode;
    private final int genotypeLength;

    public CgpGenotypeFactory(CgpConfig config, List<Function> functionSet, int numInputs, int numOutputs, RandomSource randomSource) {
        this.config = config;
        this.functionSet = functionSet;
        this.numInputs = numInputs;
        this.numOutputs = numOutputs;
        this.random = randomSource.generator();
        this.functionArity = functionSet.isEmpty() ? 2 : functionSet.stream().mapToInt(Function::getArity).max().orElse(2);
        this.genesPerNode = this.functionArity + 1;
        this.numNodes = config.getRows() * config.getCols();
        this.genotypeLength = this.numNodes * this.genesPerNode + this.numOutputs;
    }

    /**
     * Creates a new random CGP genotype.
     *
     * @return A new int[] genotype.
     */
    public int[] create() {
        int[] genotype = new int[genotypeLength];
        int geneIndex = 0;

        for (int i = 0; i < numNodes; i++) {
            int currentCol = i / config.getRows();

            // Create connection genes
            for (int j = 0; j < functionArity; j++) {
                int minCol = 0;
                if (config.getLevelsBack() > 0) {
                    minCol = Math.max(0, currentCol - config.getLevelsBack());
                }

                int maxNodeIndex = currentCol * config.getRows();
                int minNodeIndex = minCol * config.getRows();

                int numConnectableNodes = maxNodeIndex - minNodeIndex;
                int numConnectable = numInputs + numConnectableNodes;

                int connection = random.nextInt(numConnectable);
                if (connection < numInputs) {
                    genotype[geneIndex++] = connection;
                } else {
                    genotype[geneIndex++] = numInputs + minNodeIndex + (connection - numInputs);
                }
            }
            // Create function gene
            genotype[geneIndex++] = random.nextInt(functionSet.size());
        }

        // Create output connection genes
        for (int i = 0; i < numOutputs; i++) {
            int lowerBound = 0; // Outputs can connect to inputs or nodes
            int upperBound = numInputs + numNodes;
            genotype[geneIndex++] = random.nextInt(upperBound - lowerBound) + lowerBound;
        }

        return genotype;
    }

    public int getGenotypeLength() {
        return genotypeLength;
    }
}
