package com.knezevic.edaf.algorithm.cgp;

import com.knezevic.edaf.algorithm.cgp.model.CgpNode;
import com.knezevic.edaf.algorithm.cgp.model.ProgramGraph;
import com.knezevic.edaf.genotype.tree.primitives.Function;

import java.util.ArrayList;
import java.util.List;

/**
 * Decodes a CGP genotype into a program graph (phenotype).
 */
public class CgpDecoder {

    private final CgpConfig config;
    private final List<Function> functionSet;
    private final int numInputs;
    private final int numOutputs;
    private final int functionArity;
    private final int genesPerNode;
    private final int numNodes;

    public CgpDecoder(CgpConfig config, List<Function> functionSet, int numInputs, int numOutputs) {
        this.config = config;
        this.functionSet = functionSet;
        this.numInputs = numInputs;
        this.numOutputs = numOutputs;
        this.functionArity = functionSet.isEmpty() ? 2 : functionSet.stream().mapToInt(Function::getArity).max().orElse(2);
        this.genesPerNode = this.functionArity + 1;
        this.numNodes = config.getRows() * config.getCols();
    }

    /**
     * Decodes the genotype of the given individual into a program graph.
     * The decoded graph is then cached in the individual's phenotype field.
     *
     * @param individual The individual to decode.
     */
    public void decode(CgpIndividual individual) {
        int[] genotype = individual.getGenotype();
        List<CgpNode> nodes = new ArrayList<>(numNodes);
        int geneIndex = 0;

        // 1. Create nodes from genotype
        for (int i = 0; i < numNodes; i++) {
            int[] inputs = new int[functionArity];
            for (int j = 0; j < functionArity; j++) {
                inputs[j] = genotype[geneIndex++];
            }
            int functionId = genotype[geneIndex++];
            Function function = functionSet.get(functionId);
            nodes.add(new CgpNode(function, inputs));
        }

        // 2. Determine output connections from genotype
        int[] outputConnections = new int[numOutputs];
        for (int i = 0; i < numOutputs; i++) {
            outputConnections[i] = genotype[geneIndex++];
        }

        // 3. Create ProgramGraph
        ProgramGraph graph = new ProgramGraph(nodes, outputConnections, numInputs);

        // 4. Mark active nodes by tracing back from outputs
        markActiveNodes(graph);

        // 5. Set the phenotype on the individual
        individual.setPhenotype(graph);
    }

    private void markActiveNodes(ProgramGraph graph) {
        List<CgpNode> nodes = graph.getNodes();
        int[] outputConnections = graph.getOutputConnections();

        // Start a recursive traversal from each output node
        for (int outputConnection : outputConnections) {
            // outputConnection is an index into the "virtual" grid of (inputs + nodes)
            if (outputConnection >= numInputs) {
                markRecursively(outputConnection - numInputs, nodes);
            }
        }
    }

    private void markRecursively(int nodeIndex, List<CgpNode> nodes) {
        if (nodeIndex < 0 || nodeIndex >= nodes.size()) {
            return;
        }

        CgpNode node = nodes.get(nodeIndex);
        if (node.isActive()) {
            // Already visited
            return;
        }

        node.setActive(true);

        for (int inputConnection : node.getInputs()) {
            // inputConnection is an index into the "virtual" grid of (inputs + nodes)
            if (inputConnection >= numInputs) {
                markRecursively(inputConnection - numInputs, nodes);
            }
        }
    }
}
