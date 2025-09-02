package com.knezevic.edaf.algorithm.gp.operators;

import com.knezevic.edaf.core.api.Crossover;
import com.knezevic.edaf.genotype.tree.Node;
import com.knezevic.edaf.genotype.tree.TreeIndividual;
import com.knezevic.edaf.genotype.tree.util.TreeUtils;

import java.util.Random;

/**
 * Performs subtree crossover for Genetic Programming.
 * It swaps a random subtree from one parent with a random subtree from another.
 */
public class TreeCrossover implements Crossover<TreeIndividual> {

    private final Random random;

    public TreeCrossover(Random random) {
        this.random = random;
    }

    @Override
    public TreeIndividual crossover(TreeIndividual parent1, TreeIndividual parent2) {
        // Create deep copies of the parents to serve as offspring
        TreeIndividual offspring1 = (TreeIndividual) parent1.copy();
        TreeIndividual offspring2 = (TreeIndividual) parent2.copy();

        // Select random crossover points
        Node crossoverPoint1 = TreeUtils.getRandomNode(offspring1.getGenotype(), random);
        Node crossoverPoint2 = TreeUtils.getRandomNode(offspring2.getGenotype(), random);

        // Swap the subtrees. This requires finding the parent and replacing the child.
        // We use a utility method for this.
        TreeUtils.replaceNode(offspring1.getGenotype(), crossoverPoint1, crossoverPoint2.copy());
        TreeUtils.replaceNode(offspring2.getGenotype(), crossoverPoint2, crossoverPoint1.copy());

        // The Crossover interface is defined to return a single individual.
        // We will return the first offspring. A different selection scheme
        // could decide which of the two offspring to return.
        return offspring1;
    }
}
