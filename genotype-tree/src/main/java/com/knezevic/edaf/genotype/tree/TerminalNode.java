package com.knezevic.edaf.genotype.tree;

import com.knezevic.edaf.genotype.tree.primitives.Terminal;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * A node in the program tree that represents a terminal (a leaf).
 */
public class TerminalNode implements Node {

    private final Terminal terminal;

    public TerminalNode(Terminal terminal) {
        this.terminal = terminal;
    }

    @Override
    public int arity() {
        return 0;
    }

    @Override
    public double evaluate(Map<String, Double> terminalValues) {
        if (terminal.isVariable()) {
            return terminalValues.getOrDefault(terminal.getName(), 0.0);
        } else {
            return terminal.getValue();
        }
    }

    @Override
    public Node copy() {
        return new TerminalNode(terminal);
    }

    @Override
    public String print() {
        return terminal.getName();
    }

    @Override
    public List<Node> getChildren() {
        return Collections.emptyList();
    }
}
