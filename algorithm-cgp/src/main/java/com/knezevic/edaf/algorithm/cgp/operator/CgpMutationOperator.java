package com.knezevic.edaf.algorithm.cgp.operator;

import com.knezevic.edaf.algorithm.cgp.CgpConfig;
import com.knezevic.edaf.algorithm.cgp.CgpIndividual;
import com.knezevic.edaf.core.api.Mutation;
import com.knezevic.edaf.core.runtime.RandomSource;
import com.knezevic.edaf.genotype.tree.primitives.Function;

import java.util.List;
import java.util.random.RandomGenerator;

/**
 * Performs mutation on a CGP individual.
 */
public class CgpMutationOperator implements Mutation<CgpIndividual> {

    private final CgpConfig config;
    private final List<Function> functionSet;
    private final int numInputs;
    private final int numOutputs;
    private final RandomGenerator random;
    private final int functionArity;
    private final int numNodes;

    public CgpMutationOperator(CgpConfig config, List<Function> functionSet, int numInputs, int numOutputs, RandomSource randomSource) {
        this.config = config;
        this.functionSet = functionSet;
        this.numInputs = numInputs;
        this.numOutputs = numOutputs;
        this.random = randomSource.generator();
        this.functionArity = functionSet.isEmpty() ? 2 : functionSet.stream().mapToInt(Function::getArity).max().orElse(2);
        this.numNodes = config.getRows() * config.getCols();
    }

    @Override
    public void mutate(CgpIndividual individual) {
        int[] genotype = individual.getGenotype();
        int geneIndex = 0;

        // Mutate node genes
        for (int i = 0; i < numNodes; i++) {
            int currentCol = i / config.getRows();

            // Mutate connection genes
            for (int j = 0; j < functionArity; j++) {
                if (random.nextDouble() < config.getMutationRate()) {
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
                        genotype[geneIndex] = connection;
                    } else {
                        genotype[geneIndex] = numInputs + minNodeIndex + (connection - numInputs);
                    }
                }
                geneIndex++;
            }
            // Mutate function gene
            if (random.nextDouble() < config.getMutationRate()) {
                genotype[geneIndex] = random.nextInt(functionSet.size());
            }
            geneIndex++;
        }

        // Mutate output connection genes
        for (int i = 0; i < numOutputs; i++) {
            if (random.nextDouble() < config.getMutationRate()) {
                int lowerBound = 0; // Outputs can connect to inputs or nodes
                int upperBound = numInputs + numNodes;
                genotype[geneIndex] = random.nextInt(upperBound - lowerBound) + lowerBound;
            }
            geneIndex++;
        }
    }
}
