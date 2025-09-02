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

    private Node grow(int currentDepth) {
        if (currentDepth >= maxDepth || random.nextBoolean()) {
            return createRandomTerminalNode();
        } else {
            Function function = primitiveSet.getRandomFunction();
            List<Node> children = new ArrayList<>();
            for (int i = 0; i < function.getArity(); i++) {
                children.add(grow(currentDepth + 1));
            }
            return new FunctionNode(function, children);
        }
    }

    private TerminalNode createRandomTerminalNode() {
        Terminal terminalTemplate = primitiveSet.getRandomTerminal();
        if (terminalTemplate.isEphemeral()) {
            double[] range = terminalTemplate.getRange();
            double value = random.nextDouble() * (range[1] - range[0]) + range[0];
            // Create a new concrete terminal with the random value
            Terminal concreteTerminal = new Terminal(String.format("%.3f", value), value);
            return new TerminalNode(concreteTerminal);
        } else {
            // It's a variable or a pre-defined constant
            return new TerminalNode(terminalTemplate);
        }
    }

    @Override
    public int getLength() {
        return maxDepth;
    }
}
