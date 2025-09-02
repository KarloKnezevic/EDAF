package com.knezevic.edaf.genotype.tree.interpreter;

import com.knezevic.edaf.genotype.tree.Node;
import com.knezevic.edaf.genotype.tree.interpreter.context.StatefulContext;
import com.knezevic.edaf.genotype.tree.primitives.Function;
import com.knezevic.edaf.genotype.tree.primitives.Terminal;

/**
 * Executes a program tree in a stateful, procedural context.
 */
public class TreeInterpreter {

    private final StatefulContext context;
    private int steps;
    private final int maxSteps;

    public TreeInterpreter(StatefulContext context, int maxSteps) {
        this.context = context;
        this.maxSteps = maxSteps;
        this.steps = 0;
    }

    public void execute(Node root) {
        while (steps < maxSteps) {
            executeNode(root);
        }
    }

    private void executeNode(Node node) {
        if (steps >= maxSteps) {
            return;
        }

        Object primitive = node.getValue();

        if (primitive instanceof Function) {
            Function func = (Function) primitive;
            switch (func.getName().toUpperCase()) {
                case "IF_FOOD_AHEAD": // This name is now too specific. The context will handle the condition.
                    if (context.checkCondition()) {
                        executeNode(node.getChildren().get(0));
                    } else {
                        executeNode(node.getChildren().get(1));
                    }
                    break;
                case "PROGN2":
                    executeNode(node.getChildren().get(0));
                    executeNode(node.getChildren().get(1));
                    break;
                case "PROGN3":
                    executeNode(node.getChildren().get(0));
                    executeNode(node.getChildren().get(1));
                    executeNode(node.getChildren().get(2));
                    break;
            }
        } else if (primitive instanceof Terminal) {
            Terminal term = (Terminal) primitive;
            context.performAction(term.getName());
            steps++;
        }
    }
}
