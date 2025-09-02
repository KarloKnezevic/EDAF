package com.knezevic.edaf.genotype.tree;

import com.knezevic.edaf.genotype.tree.primitives.Function;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A node in the program tree that represents a function.
 */
public class FunctionNode implements Node {

    private final Function function;
    private final List<Node> children;

    public FunctionNode(Function function, List<Node> children) {
        if (function.getArity() != children.size()) {
            throw new IllegalArgumentException("Function arity does not match number of children.");
        }
        this.function = function;
        this.children = new ArrayList<>(children); // Ensure mutable list
    }

    @Override
    public int arity() {
        return function.getArity();
    }

    @Override
    public double evaluate(Map<String, Double> terminalValues) {
        double[] childResults = new double[children.size()];
        for (int i = 0; i < children.size(); i++) {
            childResults[i] = children.get(i).evaluate(terminalValues);
        }
        return function.getOp().apply(childResults);
    }

    @Override
    public Node copy() {
        List<Node> copiedChildren = new ArrayList<>();
        for (Node child : children) {
            copiedChildren.add(child.copy());
        }
        return new FunctionNode(function, copiedChildren);
    }

    @Override
    public String print() {
        return function.getName() + "(" +
                children.stream().map(Node::print).collect(Collectors.joining(",")) +
                ")";
    }

    @Override
    public List<Node> getChildren() {
        return children;
    }

    @Override
    public Object getValue() {
        return function;
    }

    /**
     * Replaces a child at a specific index.
     * @param index The index of the child to replace.
     * @param newChild The new child node.
     */
    public void setChild(int index, Node newChild) {
        if (index >= 0 && index < children.size()) {
            children.set(index, newChild);
        }
    }
}
