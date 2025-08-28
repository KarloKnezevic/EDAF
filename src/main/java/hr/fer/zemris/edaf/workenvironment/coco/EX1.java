package hr.fer.zemris.edaf.workenvironment.coco;

import hr.fer.zemris.edaf.algorithm.Algorithm;
import hr.fer.zemris.edaf.fitness.bbob.BBOB;
import hr.fer.zemris.edaf.genotype.Individual;
import hr.fer.zemris.edaf.workenvironment.Evaluation;

/**
 * COmparing Continuous Optimisers: COCO
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class EX1 extends Evaluation {

	private BBOB generic;

	public EX1(String[] args) {
		super(args);
	}

	@Override
	public void init(Algorithm algorithm) {

		generic = new BBOB(1, 0, 30);
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
