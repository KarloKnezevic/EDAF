package com.knezevic.edaf.algorithm.cgp.model;

import java.util.Arrays;
import java.util.List;

/**
 * Represents the decoded phenotype of a CGP individual.
 * This is a directed acyclic graph (DAG) that can be executed.
 */
public class ProgramGraph {

    private final List<CgpNode> nodes;
    private final int[] outputConnections;
    private final int numInputs;

    public ProgramGraph(List<CgpNode> nodes, int[] outputConnections, int numInputs) {
        this.nodes = nodes;
        this.outputConnections = outputConnections;
        this.numInputs = numInputs;
    }

    public List<CgpNode> getNodes() {
        return nodes;
    }

    public int[] getOutputConnections() {
        return outputConnections;
    }

    public double[] execute(double[] inputs) {
        if (inputs.length != numInputs) {
            throw new IllegalArgumentException("Expected " + numInputs + " inputs, but got " + inputs.length);
        }

        // Evaluate nodes in order
        for (CgpNode node : nodes) {
            if (node.isActive()) {
                double[] nodeInputs = new double[node.getFunction().getArity()];
                for (int i = 0; i < node.getFunction().getArity(); i++) {
                    int connection = node.getInputs()[i];
                    if (connection < numInputs) {
                        // It's a program input
                        nodeInputs[i] = inputs[connection];
                    } else {
                        // It's a connection to another node
                        nodeInputs[i] = nodes.get(connection - numInputs).getValue();
                    }
                }
                node.setValue(node.getFunction().getOp().apply(nodeInputs));
            }
        }

        // Get the final outputs
        double[] outputs = new double[outputConnections.length];
        for (int i = 0; i < outputConnections.length; i++) {
            int connection = outputConnections[i];
            if (connection < numInputs) {
                outputs[i] = inputs[connection];
            } else {
                outputs[i] = nodes.get(connection - numInputs).getValue();
            }
        }
        return outputs;
    }
}
