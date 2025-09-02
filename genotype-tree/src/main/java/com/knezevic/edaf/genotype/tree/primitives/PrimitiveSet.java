package com.knezevic.edaf.genotype.tree.primitives;

import java.util.List;
import java.util.Map;

/**
 * Holds the set of functions and terminals available for creating program trees.
 */
public class PrimitiveSet {

    private final List<Function> functions;
    private final List<Terminal> terminals;
    private final Map<String, Double> terminalValues;

    /**
     * Constructs a new PrimitiveSet.
     *
     * @param functions      The list of available functions.
     * @param terminals      The list of available terminals (variables and constants).
     * @param terminalValues A map to hold the current values of variable terminals.
     */
    public PrimitiveSet(List<Function> functions, List<Terminal> terminals, Map<String, Double> terminalValues) {
        this.functions = functions;
        this.terminals = terminals;
        this.terminalValues = terminalValues;
    }

    public List<Function> getFunctions() {
        return functions;
    }

    public List<Terminal> getTerminals() {
        return terminals;
    }

    public Map<String, Double> getTerminalValues() {
        return terminalValues;
    }

    public Function getRandomFunction() {
        return functions.get((int) (Math.random() * functions.size()));
    }

    public Terminal getRandomTerminal() {
        return terminals.get((int) (Math.random() * terminals.size()));
    }
}
