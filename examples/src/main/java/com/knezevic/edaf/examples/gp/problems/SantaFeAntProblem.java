package com.knezevic.edaf.examples.gp.problems;

import com.knezevic.edaf.core.api.Problem;
import com.knezevic.edaf.examples.gp.ant.Ant;
import com.knezevic.edaf.examples.gp.ant.AntContext;
import com.knezevic.edaf.examples.gp.ant.Grid;
import com.knezevic.edaf.genotype.tree.TreeIndividual;
import com.knezevic.edaf.genotype.tree.interpreter.TreeInterpreter;
import com.knezevic.edaf.genotype.tree.interpreter.context.StatefulContext;

/**
 * Implements the Santa Fe Ant Trail problem.
 * The fitness of an individual is the number of food pellets eaten by the ant
 * when executing the individual's program tree.
 */
public class SantaFeAntProblem implements Problem<TreeIndividual> {

    private static final int MAX_STEPS = 600;

    @Override
    public void evaluate(TreeIndividual individual) {
        Grid grid = new Grid();
        Ant ant = new Ant(grid);
        StatefulContext context = new AntContext(ant);
        TreeInterpreter interpreter = new TreeInterpreter(context, MAX_STEPS);

        // Execute the program tree
        interpreter.execute(individual.getGenotype());

        // Fitness is the number of food pellets eaten. Higher is better.
        // The framework assumes lower fitness is better, so we invert the value.
        int foodEaten = grid.getFoodEaten();
        // The maximum possible food is 89.
        individual.setFitness(89 - foodEaten);
    }
}
