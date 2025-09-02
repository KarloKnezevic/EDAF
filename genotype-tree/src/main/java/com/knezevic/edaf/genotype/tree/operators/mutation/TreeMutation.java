package com.knezevic.edaf.genotype.tree.operators.mutation;

import com.knezevic.edaf.core.api.Mutation;
import com.knezevic.edaf.genotype.tree.Node;
import com.knezevic.edaf.genotype.tree.TreeGenotype;
import com.knezevic.edaf.genotype.tree.TreeIndividual;
import com.knezevic.edaf.genotype.tree.primitives.PrimitiveSet;
import com.knezevic.edaf.genotype.tree.util.TreeUtils;

import java.util.Random;

/**
 * Performs subtree mutation for Genetic Programming.
 * It replaces a random subtree with a new, randomly generated subtree.
 */
public class TreeMutation implements Mutation<TreeIndividual> {

    private final TreeGenotype subtreeGenerator;
    private final Random random;

    /**
     * Constructs a TreeMutation operator.
     * @param primitiveSet The set of primitives to use for generating new subtrees.
     * @param maxDepth The maximum depth for generated subtrees.
     * @param random A random number generator.
     */
    public TreeMutation(PrimitiveSet primitiveSet, int maxDepth, Random random) {
        this.random = random;
        // Mutated subtrees should be smaller to control bloat
        int mutationMaxDepth = maxDepth / 2;
        if (mutationMaxDepth < 1) {
            mutationMaxDepth = 1;
        }
        this.subtreeGenerator = new TreeGenotype(primitiveSet, mutationMaxDepth, random);
    }

    @Override
    public void mutate(TreeIndividual individual) {
        Node root = individual.getGenotype();
        if (root == null) return;

        Node mutationPoint = TreeUtils.getRandomNode(root, random);
        Node newSubtree = subtreeGenerator.create();

        if (root != mutationPoint) {
            TreeUtils.replaceNode(root, mutationPoint, newSubtree);
        }
    }
}
