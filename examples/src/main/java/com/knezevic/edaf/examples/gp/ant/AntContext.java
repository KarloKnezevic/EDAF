package com.knezevic.edaf.examples.gp.ant;

import com.knezevic.edaf.genotype.tree.interpreter.context.StatefulContext;

/**
 * A stateful context implementation for the Santa Fe Ant Trail problem.
 * It connects the generic TreeInterpreter to the specific Ant and Grid state.
 */
public class AntContext implements StatefulContext {

    private final Ant ant;

    public AntContext(Ant ant) {
        this.ant = ant;
    }

    @Override
    public boolean checkCondition() {
        // For this problem, the only condition is "is food ahead?"
        return ant.isFoodAhead();
    }

    @Override
    public void performAction(String actionName) {
        switch (actionName.toUpperCase()) {
            case "MOVE":
                ant.move();
                break;
            case "LEFT":
                ant.turnLeft();
                break;
            case "RIGHT":
                ant.turnRight();
                break;
        }
    }
}
