package hr.fer.zemris.edaf.workenvironment.minFunction;

import hr.fer.zemris.edaf.algorithm.Algorithm;
import hr.fer.zemris.edaf.fitness.multimodal.IMultimodal;
import hr.fer.zemris.edaf.fitness.multimodal.SchwefelsFunction;
import hr.fer.zemris.edaf.genotype.Individual;
import hr.fer.zemris.edaf.workenvironment.Evaluation;

/**
 * EX1.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class EX1 extends Evaluation {

	private IMultimodal f;

	public EX1(String[] args) {
		super(args);
	}

	@Override
	public void init(Algorithm algorithm) {

		f = new SchwefelsFunction();
		// f = new GriewanksFunction();

		algorithm.run();

	}

	@Override
	public void evaluate(Individual individual) {

		individual.setFitness(f.computeMultimodal(individual.getVariable()));

	}

}