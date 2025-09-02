package com.knezevic.edaf.genotype.tree;

import com.knezevic.edaf.core.api.Genotype;
import com.knezevic.edaf.genotype.tree.primitives.Function;
import com.knezevic.edaf.genotype.tree.primitives.PrimitiveSet;
import com.knezevic.edaf.genotype.tree.primitives.Terminal;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A factory for creating tree-based genotypes (program trees).
 * This class implements the logic for generating random trees.
 */
public class TreeGenotype implements Genotype<Node> {

    private final PrimitiveSet primitiveSet;
    private final int maxDepth;
    private final Random random;

    public TreeGenotype(PrimitiveSet primitiveSet, int maxDepth, Random random) {
        this.primitiveSet = primitiveSet;
        this.maxDepth = maxDepth;
        this.random = random;
    }

    @Override
    public Node create() {
        return grow(0);
    }

    /**
     * The "grow" method for generating a random tree.
     * It can create trees of varying shapes.
     * @param currentDepth The current depth in the tree construction.
     * @return The root node of the newly generated subtree.
     */
    private Node grow(int currentDepth) {
        // At max depth, must choose a terminal
        if (currentDepth >= maxDepth) {
            Terminal terminal = primitiveSet.getRandomTerminal();
            return new TerminalNode(terminal);
        }

        // At other depths, can choose a function or a terminal
        if (random.nextBoolean()) { // 50% chance to choose a terminal
            Terminal terminal = primitiveSet.getRandomTerminal();
            return new TerminalNode(terminal);
        } else { // 50% chance to choose a function
            Function function = primitiveSet.getRandomFunction();
            List<Node> children = new ArrayList<>();
            for (int i = 0; i < function.getArity(); i++) {
                children.add(grow(currentDepth + 1));
            }
            return new FunctionNode(function, children);
        }
    }

    @Override
    public int getLength() {
        // The concept of "length" is ambiguous for a tree.
        // We can return the max depth or -1 to indicate it's not applicable.
        return maxDepth;
    }
}
