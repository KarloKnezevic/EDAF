package hr.fer.zemris.edaf.workenvironment.coco;

import hr.fer.zemris.edaf.algorithm.Algorithm;
import hr.fer.zemris.edaf.fitness.bbob.BBOB;
import hr.fer.zemris.edaf.genotype.Individual;
import hr.fer.zemris.edaf.workenvironment.Evaluation;

/**
 * TEST BBOB Expertiments for Master Thesis.
 * 
 * Functions: 1-24 Instance: 1 Dimension: 30
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class TEST extends Evaluation {

	private BBOB generic;

	public TEST(String[] args) {
		super(args);
	}

	@Override
	public void init(Algorithm algorithm) {
		// 2. arg from command line if function num (1-24)
		final int function = Integer.parseInt(cmdArgs[1]);

		generic = new BBOB(function, 1, 30);
		generic.init();

		algorithm.run();

	}

	@Override
	public void evaluate(Individual individual) {
		/*
		 * subtract optimum offset for standardization
		 */
		individual.setFitness(generic.evaluate(individual.getVariable())
				- generic.getBenchmark().getFadd());

	}

}
