package com.knezevic.edaf.genotype.tree;

import java.util.List;
import java.util.Map;

/**
 * Represents a node in the program tree for Genetic Programming.
 * A node can be either a function or a terminal.
 */
public interface Node {
    /**
     * Returns the number of children (arguments) this node has.
     * Terminals have an arity of 0.
     *
     * @return the arity of the node.
     */
    int arity();

    /**
     * Evaluates the subtree rooted at this node.
     *
     * @param terminalValues A map containing the values for variable terminals.
     * @return the result of the evaluation.
     */
    double evaluate(Map<String, Double> terminalValues);

    /**
     * Creates a deep copy of the node and its entire subtree.
     *
     * @return a new Node instance that is a deep copy of this one.
     */
    Node copy();

    /**
     * Returns a string representation of the subtree rooted at this node.
     * e.g., "ADD(x, 5.0)"
     *
     * @return a string representation of the program tree.
     */
    String print();

    /**
     * Returns a list of the node's children.
     * For terminals, this will be an empty list.
     *
     * @return a list of child nodes.
     */
    List<Node> getChildren();
}
