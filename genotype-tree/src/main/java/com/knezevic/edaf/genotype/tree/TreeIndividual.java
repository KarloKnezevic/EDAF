package com.knezevic.edaf.genotype.tree;

import com.knezevic.edaf.core.api.Individual;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an individual solution in Genetic Programming.
 * The genotype is a program tree, represented by its root Node.
 */
public class TreeIndividual implements Individual<Node> {

    private final Node genotype;
    private double fitness;

    public TreeIndividual(Node genotype) {
        this.genotype = genotype;
        this.fitness = Double.MAX_VALUE;
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
    public Node getGenotype() {
        return genotype;
    }

    @Override
    public Individual<Node> copy() {
        TreeIndividual copied = new TreeIndividual(this.genotype.copy());
        copied.setFitness(this.fitness);
        return copied;
    }

    @Override
    public String toString() {
        return "TreeIndividual{" +
                "fitness=" + fitness +
                ", tree=" + genotype.print() +
                '}';
    }

    /**
     * Helper method to get all nodes in the tree in a flattened list.
     * Useful for crossover and mutation.
     * @return A list of all nodes in the tree.
     */
    public List<Node> getAllNodes() {
        List<Node> nodes = new ArrayList<>();
        collectNodes(genotype, nodes);
        return nodes;
    }

    private void collectNodes(Node node, List<Node> nodes) {
        nodes.add(node);
        for (Node child : node.getChildren()) {
            collectNodes(child, nodes);
        }
    }
}
