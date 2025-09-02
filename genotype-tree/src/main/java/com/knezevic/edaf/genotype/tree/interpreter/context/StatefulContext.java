package com.knezevic.edaf.genotype.tree.interpreter.context;

/**
 * An interface for providing state and actions to a TreeInterpreter.
 * This allows the interpreter to be generic and not tied to a specific problem.
 */
public interface StatefulContext {
    /**
     * A conditional check for the 'IF' type functions.
     * @return true if the condition is met, false otherwise.
     */
    boolean checkCondition();

    /**
     * Perform a named action.
     * @param actionName The name of the action to perform (e.g., "MOVE", "LEFT").
     */
    void performAction(String actionName);
}
