package com.knezevic.edaf.testing.problems.gp;

import com.knezevic.edaf.core.impl.AbstractProblem;
import com.knezevic.edaf.genotype.tree.TreeIndividual;
import com.knezevic.edaf.genotype.tree.interpreter.TreeInterpreter;
import com.knezevic.edaf.genotype.tree.interpreter.context.StatefulContext;
import com.knezevic.edaf.testing.problems.gp.ant.*;

public class SantaFeAntProblem extends AbstractProblem<TreeIndividual> {
    private static final int MAX_STEPS = 600;
    public SantaFeAntProblem(java.util.Map<String, Object> params) { super(params); }

    @Override
    public void evaluate(TreeIndividual individual) {
        Grid grid = new Grid();
        Ant ant = new Ant(grid);
        StatefulContext context = new AntContext(ant);
        TreeInterpreter interpreter = new TreeInterpreter(context, MAX_STEPS);
        interpreter.execute(individual.getGenotype());
        int foodEaten = grid.getFoodEaten();
        individual.setFitness(foodEaten);
    }
}


