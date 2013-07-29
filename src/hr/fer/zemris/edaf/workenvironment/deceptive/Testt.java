package hr.fer.zemris.edaf.workenvironment.deceptive;

import hr.fer.zemris.edaf.algorithm.Algorithm;
import hr.fer.zemris.edaf.fitness.tfon.TestFunctionsForOptimizationNeeds;
import hr.fer.zemris.edaf.genotype.Individual;
import hr.fer.zemris.edaf.workenvironment.Evaluation;

/**
 * TEST Expertiments for Master Thesis Function: deceptive Domain: 0-1 (float)
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class Testt extends Evaluation {

	TestFunctionsForOptimizationNeeds deceptive;

	public Testt(String[] args) {
		super(args);
	}

	@Override
	public void init(Algorithm algorithm) {

		deceptive = new TestFunctionsForOptimizationNeeds();

		algorithm.run();

	}

	@Override
	public void evaluate(Individual individual) {

		individual.setFitness(deceptive.Deceptive(individual.getVariable()));

	}

}
