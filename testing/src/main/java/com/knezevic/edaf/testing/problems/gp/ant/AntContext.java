package com.knezevic.edaf.testing.problems.gp.ant;

import com.knezevic.edaf.genotype.tree.interpreter.context.StatefulContext;

public class AntContext implements StatefulContext {

    private final Ant ant;

    public AntContext(Ant ant) { this.ant = ant; }

    @Override
    public boolean checkCondition() { return ant.isFoodAhead(); }

    @Override
    public void performAction(String actionName) {
        switch (actionName.toUpperCase()) {
            case "MOVE": ant.move(); break;
            case "LEFT": ant.turnLeft(); break;
            case "RIGHT": ant.turnRight(); break;
        }
    }
}


